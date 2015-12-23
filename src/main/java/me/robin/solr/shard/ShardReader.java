package me.robin.solr.shard;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.solr.common.cloud.SolrZkClient;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.Watcher;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Lubin.Xuan on 2015/12/23.
 */
public class ShardReader {

    private static final String PATH = "/implicit/";

    private SolrZkClient zkClient;

    public ShardReader(String zkHost) {
        this.zkClient = new SolrZkClient(zkHost, 10000);
    }

    public ShardReader(SolrZkClient zkClient) {
        this.zkClient = zkClient;
    }

    private void checkNode(String collection) throws KeeperException, InterruptedException {
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
                    String start = range.getString("start");
                    String end = range.getString("end");
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

    public void appendWatcher(Watcher watcher) {
        zkClient.getSolrZooKeeper().register(watcher);
    }

    public void addShardConfig(String collection, String shard, String start, String end) throws KeeperException, InterruptedException, UnsupportedEncodingException {
        byte[] data = read(collection);
        JSONObject object;
        if (null != data) {
            object = JSON.parseObject(data, JSONObject.class);
            if (object.containsKey(shard)) {
                return;
            }
        } else {
            object = new JSONObject();
        }
        JSONObject s = new JSONObject();
        s.put("start", start);
        s.put("end", end);
        object.put(shard, s);
        saveDate(collection, object);
    }

    public void addShardConfig(String collection, List<Shard> shardList) throws KeeperException, InterruptedException, UnsupportedEncodingException {
        byte[] data = read(collection);
        JSONObject object;
        if (null != data) {
            object = JSON.parseObject(data, JSONObject.class);
        } else {
            object = new JSONObject();
        }
        int update = 0;
        for (Shard shard : shardList) {
            if (object.containsKey(shard.getShard())) {
                continue;
            }
            JSONObject s = new JSONObject();
            s.put("start", shard.getStart());
            s.put("end", shard.getEnd());
            object.put(shard.getShard(), s);
            update++;
        }
        if (update == 0) {
            return;
        }
        saveDate(collection, object);
    }

    private void saveDate(String collection, JSONObject object) throws UnsupportedEncodingException, KeeperException, InterruptedException {
        zkClient.setData(PATH + collection, JSONObject.toJSONString(object, true).getBytes("utf-8"), true);
    }

    public byte[] read(String collection) throws KeeperException, InterruptedException {
        checkNode(collection);
        return zkClient.getData(PATH + collection, null, null, true);
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

    public static class ShardRouter {
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
            shardReader.appendWatcher(watchedEvent -> {
                if (Watcher.Event.EventType.NodeDataChanged.equals(watchedEvent.getType())) {
                    try {
                        update(shardReader, collection);
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                }
            });
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
                if (c1 >= 0 && c2 <= 0) {
                    return shard.getShard();
                }
            }
            return null;
        }
    }

}
