package me.robin.solr.shard;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.solr.common.cloud.SolrZkClient;
import org.apache.zookeeper.*;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

/**
 * Created by Lubin.Xuan on 2015/12/23.
 */
public class ShardRouter {
    private List<Shard> shardList;

    public ShardRouter(List<Shard> shardList) {
        this.shardList = Collections.unmodifiableList(shardList);
    }

    public ShardRouter(SolrZkClient zkClient, String collection) throws InterruptedException, UnsupportedEncodingException, KeeperException {
        this(new ShardReader(zkClient), collection);
    }

    public ShardRouter(String zkHost, String collection) throws InterruptedException, UnsupportedEncodingException, KeeperException {
        this(new ShardReader(zkHost), collection);
    }

    public ShardRouter(ShardReader shardReader, String collection) throws InterruptedException, UnsupportedEncodingException, KeeperException {

        shardReader.monitor(watchedEvent -> {
            if (!watchedEvent.getPath().equals(ShardReader.PATH + collection)) {
                return false;
            }

            if (Watcher.Event.EventType.NodeDataChanged.equals(watchedEvent.getType())) {
                try {
                    update(shardReader, collection);
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
            return true;
        }, collection);
        update(shardReader, collection);
    }

    private void update(ShardReader shardReader, String collection) throws InterruptedException, UnsupportedEncodingException, KeeperException {
        List<Shard> shardList = shardReader.listShard(collection);
        this.shardList = Collections.unmodifiableList(shardList);
    }

    public String locateShard(String routeValue) {
        for (Shard shard : shardList) {
            int c1 = null == shard.getStart() ? 1 : routeValue.compareTo(shard.getStart());
            int c2 = null == shard.getEnd() ? -1 : routeValue.compareTo(shard.getEnd());
            if (c1 >= 0 && c2 < 0) {
                return shard.getShard();
            }
        }
        return null;
    }

    /**
     * Created by Lubin.Xuan on 2015/12/23.
     */
    public static class ShardReader {

        protected static final String PATH = "/implicit/";
        protected static final String START = "start", END = "end";

        protected SolrZkClient zkClient;

        public ShardReader(String zkHost) {
            this.zkClient = new SolrZkClient(zkHost, 10000);
        }

        public ShardReader(SolrZkClient zkClient) {
            this.zkClient = zkClient;
        }

        protected void checkNode(String collection) throws KeeperException, InterruptedException {
            if (!zkClient.exists(PATH + collection, true)) {
                zkClient.makePath(PATH + collection, null, CreateMode.PERSISTENT, true);
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

        public void monitor(Predicate<WatchedEvent> watcher, String collection) throws KeeperException, InterruptedException {
            ZooKeeper zooKeeper = zkClient.getSolrZooKeeper();
            zooKeeper.exists(PATH + collection, watchedEvent -> {
                if (watcher.test(watchedEvent)) {
                    try {
                        zooKeeper.exists(watchedEvent.getPath(), true);
                    } catch (KeeperException | InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });
        }

        protected byte[] read(String collection) throws KeeperException, InterruptedException {
            checkNode(collection);
            return zkClient.getData(PATH + collection, null, null, true);
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
}