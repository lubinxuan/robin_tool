package me.robin.solr.shard;

import org.apache.commons.lang3.StringUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.impl.CloudSolrClient;
import org.apache.solr.common.cloud.DocCollection;
import org.apache.solr.common.cloud.DocRouter;
import org.apache.solr.common.cloud.ImplicitDocRouter;
import org.apache.solr.common.cloud.Slice;
import org.apache.solr.common.params.CommonParams;
import org.apache.solr.common.params.ShardParams;
import org.apache.solr.common.params.SolrParams;
import org.apache.zookeeper.KeeperException;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Created by Lubin.Xuan on 2016/2/22.
 */
public class MyImplicitDocRouter extends ImplicitDocRouter {

    private ShardRouter shardRouter;
    private ShardRouter.ShardRouterKeyParser routerKeyParser;

    private MyImplicitDocRouter(ShardRouter shardRouter, ShardRouter.ShardRouterKeyParser routerKeyParser) {
        this.shardRouter = shardRouter;
        this.routerKeyParser = routerKeyParser;
    }

    @Override
    public Collection<Slice> getSearchSlicesSingle(String shardKey, SolrParams params, DocCollection collection) {
        if (null == shardKey && shardRouter.isImplicit(collection.getName())) {
            String q = params.get(CommonParams.Q);
            String fq = params.get(CommonParams.FQ);
            String[] keyValue = routerKeyParser.parse(collection.getName(), q, fq);
            Collection<String> shardSet = shardRouter.selectQueryShard(collection.getName(), keyValue[0], keyValue[1]);
            List<Slice> sliceList = new ArrayList<>();
            for (String shard : shardSet) {
                Slice slice = collection.getSlice(shard);
                if (null != slice) {
                    sliceList.add(slice);
                }
            }
            if (!shardSet.isEmpty() && params instanceof SolrQuery) {
                ((SolrQuery) params).add(ShardParams.SHARDS, StringUtils.join(shardSet, ","));
            }
            return sliceList;
        } else {
            return super.getSearchSlicesSingle(shardKey, params, collection);
        }
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
}
