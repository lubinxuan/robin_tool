import me.robin.solr.shard.ShardConfigHelper;
import me.robin.solr.shard.ShardRouter;
import org.apache.zookeeper.KeeperException;

import java.io.IOException;

/**
 * Created by Lubin.Xuan on 2015/12/23.
 */
public class SolrShardConfig {
    public static void main(String[] args) throws InterruptedException, IOException, KeeperException {
        ShardRouter.ShardReader shardReader = new ShardRouter.ShardReader("172.16.2.30:3181,172.16.2.31:3181,172.16.2.32:3181");
        //shardReader.addShardConfig("admonitor", "2013", null, "2014-01-01");
        //shardReader.addShardConfig("admonitor", "2014_1_6", "2014-01-01", "2014-07-01");
        //shardReader.addShardConfig("admonitor", "2014_7_10", "2014-07-01", "2014-11-01");
        //shardReader.addShardConfig("admonitor", "2014_11_12", "2014-11-01", "2015-01-01");
        //shardReader.addShardConfig("admonitor", "2015_1_3", "2015-01-01", "2015-04-01");
        //shardReader.addShardConfig("admonitor", "2015_4_5", "2015-04-01", "2015-06-01");
        //shardReader.addShardConfig("admonitor", "2015_6_7", "2015-06-01", "2015-08-01");
        //shardReader.addShardConfig("admonitor", "2015_8", "2015-08-01", "2015-09-01");
        //shardReader.addShardConfig("admonitor", "2015_9", "2015-09-01", "2015-10-01");
        //shardReader.addShardConfig("admonitor", "2015_10", "2015-10-01", "2015-11-01");
        //shardReader.addShardConfig("admonitor", "2015_11", "2015-11-01", "2015-12-01");
        //shardReader.addShardConfig("admonitor", "2015_12", "2015-12-01", "2016-01-01");
        ShardConfigHelper helper = new ShardConfigHelper(shardReader.getZooKeeper());
        helper.addShardConfig("admonitor", "2016_2", "2016-02-01", "2016-03-01");
        helper.delShardConfig("admonitor", "2016_2");
        /*ShardRouter shardRouter = new ShardRouter(shardReader, "admonitor");
        long s = System.currentTimeMillis();
        CountDownLatch latch = new CountDownLatch(10);
        for (int i = 0; i < 10; i++) {
            new Thread(()->{
                try {
                    for (int j = 0; j < 1000000; j++) {
                        System.out.println(shardRouter.locateShard("2016-01-25"));
                    }
                }finally {
                    latch.countDown();
                }
            }).start();
        }
        latch.await();
        System.out.println(System.currentTimeMillis() - s);*/
    }
}
