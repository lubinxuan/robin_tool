package me.robin.solr.shard;

import org.apache.commons.lang3.StringUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.impl.CloudSolrClient;
import org.apache.solr.common.cloud.*;
import org.apache.solr.common.params.CommonParams;
import org.apache.solr.common.params.ShardParams;
import org.apache.solr.common.params.SolrParams;
import org.apache.zookeeper.KeeperException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;

/**
 * Created by Lubin.Xuan on 2016/2/22.
 */
public class MyImplicitDocRouter extends ImplicitDocRouter {

    private static final Logger logger = LoggerFactory.getLogger(MyImplicitDocRouter.class);

    private ShardRouter shardRouter;
    private ShardRouter.ShardRouterKeyParser routerKeyParser;

    private MyImplicitDocRouter(ShardRouter shardRouter, ShardRouter.ShardRouterKeyParser routerKeyParser) {
        this.shardRouter = shardRouter;
        this.routerKeyParser = routerKeyParser;
    }

    private Map<String, String> shardSliceCache = new ConcurrentHashMap<>();

    private static final ThreadLocal<Collection<Slice>> SLICES_CURRENT = new ThreadLocal<>();

    public static Collection<Slice> currentSlices() {
        return SLICES_CURRENT.get();
    }

    @Override
    public Collection<Slice> getSearchSlicesSingle(String shardKey, SolrParams params, DocCollection collection) {

        SLICES_CURRENT.remove();

        Collection<Slice> sliceCollection;

        if (StringUtils.isNotBlank(params.get(ShardParams.SHARDS))) {
            Set<String> shardNameSet = new HashSet<>();
            List<Slice> sliceList = new ArrayList<>();
            String shardInfo = params.get(ShardParams.SHARDS);
            String[] shardArr = shardInfo.split(",");
            Collection<Slice> activeSlices = collection.getActiveSlices();
            for (String shard : shardArr) {
                if (shard.contains("/solr/")) {
                    String replicaServers[] = shard.split("\\|");
                    replicaServersLoop:
                    for (String replicaSer : replicaServers) {
                        String sliceCacheName = shardSliceCache.get(replicaSer);
                        Slice tmp = collection.getSlice(sliceCacheName);
                        if (tmp != null && tmp.getState() == Slice.State.ACTIVE) {
                            sliceList.add(tmp);
                            shardNameSet.add(tmp.getName());
                            break;
                        }
                        for (Slice slice : activeSlices) {
                            Collection<Replica> replicas = slice.getReplicas();
                            for (Replica replica : replicas) {
                                if ((replica.getStr(ZkStateReader.BASE_URL_PROP) + "/" + collection.getName()).contains(replicaSer)) {
                                    sliceList.add(slice);
                                    shardNameSet.add(slice.getName());
                                    shardSliceCache.put(replicaSer, slice.getName());
                                    break replicaServersLoop;
                                }
                            }
                        }
                    }
                } else {
                    Slice slice = collection.getSlice(shard);
                    if (null != slice) {
                        sliceList.add(slice);
                        shardNameSet.add(shard);
                    }
                }
            }
            if (!shardNameSet.isEmpty()) {
                ((SolrQuery) params).set(ShardParams.SHARDS, StringUtils.join(shardNameSet, ","));
                logger.debug("查询分片节点:{} {}", collection.getName(), shardNameSet);
            }

            sliceCollection = sliceList;
        } else if (null == shardKey && shardRouter.isImplicit(collection.getName())) {
            String q = params.get(CommonParams.Q);
            String fq = params.get(CommonParams.FQ);
            String[] keyValue = routerKeyParser.parse(collection.getName(), q, fq);
            Collection<String> shardSet = shardRouter.selectQueryShard(collection.getName(), keyValue[0], keyValue[1]);
            List<String> shardList = new ArrayList<>();
            List<Slice> sliceList = new ArrayList<>();

            for (String shard : shardSet) {
                Slice slice = collection.getSlice(shard);
                if (null != slice) {
                    sliceList.add(slice);
                    Replica leader = slice.getLeader();
                    Replica target = null;
                    for (Iterator<Replica> iterator = slice.getReplicas().iterator(); iterator.hasNext(); ) {
                        Replica replica = iterator.next();

                        if (replica.equals(leader)) {
                            continue;
                        }

                        if (!Replica.State.ACTIVE.equals(replica.getState())) {
                            continue;
                        }

                        target = replica;
                    }

                    if (target == null) {
                        shardList.add(leader.getCoreUrl());
                    } else {

                    }
                }
            }

            if (params instanceof SolrQuery) {

                if (!shardSet.isEmpty()) {
                    ((SolrQuery) params).set(ShardParams.SHARDS, StringUtils.join(shardSet, ","));
                    logger.debug("查询分片节点:{} {}", collection.getName(), shardSet);
                }

            }

            sliceCollection = sliceList;
        } else {
            sliceCollection = super.getSearchSlicesSingle(shardKey, params, collection);
        }
        if (params instanceof SolrQuery) {
            if (sliceCollection.size() == 1) {
                //直连查询
                ((SolrQuery) params).add(CommonParams.DISTRIB, "false");
            }
        }

        SLICES_CURRENT.set(sliceCollection);

        return sliceCollection;
    }


    public static void init(ShardRouter router, ShardRouter.ShardRouterKeyParser routerKeyParser) throws NoSuchFieldException, IllegalAccessException {
        Field field = DocRouter.class.getDeclaredField("routerMap");
        field.setAccessible(true);
        Map<String, DocRouter> routerMap = (Map<String, DocRouter>) field.get(DocRouter.class);
        routerMap.put(ImplicitDocRouter.NAME, new MyImplicitDocRouter(router, routerKeyParser));
        field.setAccessible(false);
    }

    public static void init(CloudSolrClient cloudSolrServer, ShardRouter.ShardRouterKeyParser routerKeyParser) throws NoSuchFieldException, IllegalAccessException, InterruptedException, UnsupportedEncodingException, KeeperException {
        init(new ShardRouter(cloudSolrServer.getZkStateReader().getZkClient().getSolrZooKeeper()), routerKeyParser);
    }

    public static String queryValue(String field, String query) {
        String f = field + ":";
        int f_l = f.length();
        int idx = query.indexOf(f);
        int e_idx = query.indexOf(" OR ", idx + f_l);
        if (e_idx < 0) {
            e_idx = query.indexOf(" AND ", idx + f_l);
        }
        if (e_idx < 0) {
            return query.substring(idx + f_l, query.length()).trim();
        } else {
            return query.substring(idx + f_l, e_idx).trim();
        }
    }

    public interface ServerSelector {
        public List<String> select(List<String> servers);
    }

}
