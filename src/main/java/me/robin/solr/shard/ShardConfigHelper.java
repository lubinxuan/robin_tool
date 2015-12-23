package me.robin.solr.shard;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.solr.common.cloud.SolrZkClient;
import org.apache.zookeeper.KeeperException;

import java.io.UnsupportedEncodingException;
import java.util.List;

/**
 * Created by Lubin.Xuan on 2015/12/23.
 */
public class ShardConfigHelper extends ShardReader {
    public ShardConfigHelper(String zkHost) {
        super(zkHost);
    }

    public ShardConfigHelper(SolrZkClient zkClient) {
        super(zkClient);
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
        s.put(START, start);
        s.put(END, end);
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
            s.put(START, shard.getStart());
            s.put(END, shard.getEnd());
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
}
