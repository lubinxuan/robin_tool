import me.robin.solr.shard.ShardConfigHelper;
import me.robin.solr.shard.ShardRouter;
import org.apache.zookeeper.KeeperException;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.BiFunction;

/**
 * Created by Lubin.Xuan on 2015/12/23.
 */
public class SolrShardConfig {

    private static final Logger logger = LoggerFactory.getLogger(SolrShardConfig.class);

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
/*        helper.delShardConfig("weibo", "2015_1_6");
        helper.delShardConfig("weibo", "2015_7_12");
        helper.addShardConfig("weibo", "2013_2014", null, "2015-01-01");
        helper.addShardConfig("weibo", "2015_1_4", "2015-01-01", "2015-05-01");
        helper.addShardConfig("weibo", "2015_5_12", "2015-05-01", "2016-01-01");
        helper.addShardConfig("weibo", "2016_1_3", "2016-01-01", "2016-04-01");*/
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


    @Test
    public void createWeekRouteInfo() throws ParseException, IOException, KeeperException, InterruptedException {
        String start = "2016-01-31";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy'W'ww");
        Date s = sdf.parse(start);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(s);
        Map<String, D> data = new TreeMap<String, D>(String::compareTo);
        for (int i = 0; i < 1000; i++) {
            String yw = sdf2.format(calendar.getTime());
            Date date = calendar.getTime();
            data.compute(yw, new BiFunction<String, D, D>() {
                @Override
                public D apply(String s, D d) {
                    if (null == d) {
                        d = new D();
                        d.s = date;
                    } else {
                        d.e = date;
                    }
                    return d;
                }
            });
            calendar.add(Calendar.DATE, 1);
        }

        List<ShardRouter.Shard> shardList = new ArrayList<>();
        for (Map.Entry<String, D> entry : data.entrySet()) {
            calendar.setTime(entry.getValue().e);
            calendar.add(Calendar.DATE,1);
            Date end = calendar.getTime();
            ShardRouter.Shard shard = new ShardRouter.Shard(entry.getKey(), sdf.format(entry.getValue().s), sdf.format(end));

            shardList.add(shard);
        }


        ShardRouter.ShardReader shardReader = new ShardRouter.ShardReader("172.16.8.33:4181");
        ShardConfigHelper helper = new ShardConfigHelper(shardReader.getZooKeeper());
        helper.addShardConfig("dmb",shardList);

        System.out.println();

    }

    class D {
        Date s, e;
    }
}
