package me.robin.solr.shard;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.solr.client.solrj.impl.CloudSolrClient;
import org.apache.solr.common.cloud.DocCollection;
import org.apache.solr.common.cloud.DocRouter;
import org.apache.solr.common.cloud.ImplicitDocRouter;
import org.apache.solr.common.cloud.Slice;
import org.apache.solr.common.params.CommonParams;
import org.apache.solr.common.params.SolrParams;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Created by Lubin.Xuan on 2015/12/23.
 * solr implicit 路由 配置读取工具
 */
public class ShardRouter {

    private static final Logger logger = LoggerFactory.getLogger(ShardRouter.class);

    private final Map<String, List<Shard>> shardMap = new ConcurrentHashMap<>();

    public ShardRouter(ZooKeeper zooKeeper) throws InterruptedException, UnsupportedEncodingException, KeeperException {
        this(new ShardReader(zooKeeper));
    }

    public ShardRouter(String zkHost) throws InterruptedException, IOException, KeeperException {
        this(new ShardReader(zkHost));
    }

    public ShardRouter(ShardReader shardReader) throws InterruptedException, UnsupportedEncodingException, KeeperException {
        shardReader.monitor(watchedEvent -> {

            if (null == watchedEvent || null == watchedEvent.getPath()) {
                return false;
            }

            if (!watchedEvent.getPath().startsWith(ShardReader.PATH)) {
                return false;
            }

            logger.debug("开始更新 Shard 配置信息 路径:{} 事件:{}", watchedEvent.getPath(), watchedEvent.getType());

            if (Watcher.Event.EventType.NodeDataChanged.equals(watchedEvent.getType())) {
                try {
                    String collection = watchedEvent.getPath().substring(ShardReader.PATH.length());
                    update(shardReader, collection);
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
            return true;
        });
    }

    private void update(ShardReader shardReader, String collection) throws InterruptedException, UnsupportedEncodingException, KeeperException {
        List<Shard> shardList = shardReader.listShard(collection);
        shardMap.put(collection, Collections.unmodifiableList(shardList));
    }

    public String locateShard(String collection, String routeValue) {
        if (!shardMap.containsKey(collection)) {
            return null;
        }
        for (Shard shard : shardMap.get(collection)) {
            if (locate(shard, routeValue)) {
                return shard.getShard();
            }
        }
        return null;
    }

    private boolean locate(Shard shard, String routeValue) {
        int c1 = null == shard.getStart() ? 1 : routeValue.compareTo(shard.getStart());
        int c2 = null == shard.getEnd() ? -1 : routeValue.compareTo(shard.getEnd());
        return c1 >= 0 && c2 < 0;
    }

    public Collection<String> selectQueryShard(String collection, String startRoute, String endRoute) {

        List<Shard> shardList = shardMap.get(collection);

        Set<String> allShard = shardList.stream().map(Shard::getShard).collect(Collectors.toSet());

        if (null == startRoute && null == endRoute) {
            return allShard;
        }

        if (StringUtils.equals(startRoute, endRoute) && null != startRoute && !"*".equals(startRoute)) {
            return Collections.singleton(locateShard(collection, startRoute));
        }


        Set<String> pre = new HashSet<>();
        Set<String> aft = new HashSet<>();
        for (Shard shard : shardList) {

            if (null != startRoute) {
                int c2 = null == shard.getEnd() ? -1 : startRoute.compareTo(shard.getEnd());
                if (c2 < 0) {
                    pre.add(shard.getShard());
                }
            }
            if (null != endRoute) {
                int c1 = null == shard.getStart() ? 1 : endRoute.compareTo(shard.getStart());
                if (c1 >= 0) {
                    aft.add(shard.getShard());
                }
            }
        }

        if (null == startRoute) {
            return aft;
        } else if (null == endRoute) {
            return pre;
        } else {
            return CollectionUtils.intersection(pre, aft);
        }
    }

    public boolean isImplicit(String collection) {
        return shardMap.containsKey(collection);
    }

    /**
     * Created by Lubin.Xuan on 2015/12/23.
     */
    public static class ShardReader {
        private static final Logger logger = LoggerFactory.getLogger(ShardRouter.class);
        protected static final String PATH = "/implicit/";
        protected static final String START = "start", END = "end";

        protected ZooKeeper zooKeeper;

        public ShardReader(String zkHost) throws IOException, KeeperException, InterruptedException {
            this.zooKeeper = new ZooKeeper(zkHost, 10000, new Watcher() {
                @Override
                public void process(WatchedEvent watchedEvent) {
                    logger.debug("回调watcher实例： 路径 {} 类型：{}", watchedEvent.getPath(), watchedEvent.getType());
                }
            });

            if (zooKeeper.exists("/implicit", false) == null) {
                zooKeeper.create("/implicit", null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            }
        }

        public ShardReader(ZooKeeper zooKeeper) {
            this.zooKeeper = zooKeeper;
        }

        protected void checkNode(String collection) throws KeeperException, InterruptedException {
            if (zooKeeper.exists(PATH + collection, false) == null) {
                zooKeeper.create(PATH + collection, null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            }
        }

        public List<Shard> listShard(String collection) throws KeeperException, InterruptedException, UnsupportedEncodingException {
            try {
                byte[] data = read(collection);
                if (null != data) {
                    JSONObject object = JSON.parseObject(data, JSONObject.class);
                    List<Shard> shardList = new ArrayList<>();
                    for (String shard : object.keySet()) {
                        JSONObject range = object.getJSONObject(shard);
                        String start = range.getString(START);
                        String end = range.getString(END);
                        shardList.add(new Shard(shard, start, end));
                    }
                    return shardList;
                } else {
                    return Collections.emptyList();
                }
            } catch (Throwable e) {
                if (e instanceof KeeperException.NoNodeException) {
                    return Collections.emptyList();
                }
                throw e;
            }
        }

        public void monitor(Predicate<WatchedEvent> predicate) throws KeeperException, InterruptedException {
            List<String> children = zooKeeper.getChildren(PATH.substring(0, PATH.length() - 1), false);
            Watcher watcher = new Watcher() {
                @Override
                public void process(WatchedEvent watchedEvent) {
                    if (predicate.test(watchedEvent)) {
                        try {
                            zooKeeper.exists(watchedEvent.getPath(), this);
                        } catch (KeeperException | InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            };
            for (String cPath : children) {
                zooKeeper.exists(PATH + cPath, watcher);
                predicate.test(new WatchedEvent(Watcher.Event.EventType.NodeDataChanged, null, PATH + cPath));
            }
        }

        protected byte[] read(String collection) throws KeeperException, InterruptedException {
            checkNode(collection);
            return zooKeeper.getData(PATH + collection, false, new Stat());
        }

        public ZooKeeper getZooKeeper() {
            return zooKeeper;
        }

    }

    /**
     * Created by Lubin.Xuan on 2015/12/23.
     */
    public static class Shard {
        private String shard;
        private String start;
        private String end;

        public Shard(String shard, String start, String end) {
            this.shard = shard;
            this.start = start;
            this.end = end;
        }

        public String getShard() {
            return shard;
        }

        public String getStart() {
            return start;
        }

        public String getEnd() {
            return end;
        }
    }

    public interface ShardRouterKeyParser {
        public String[] parse(String collection, String q, String fq);
    }

}