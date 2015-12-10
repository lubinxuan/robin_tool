import me.robin.solr.util.HBaseSolrData;
import me.robin.solr.util.RowKeyGenerator;
import me.robin.solr.util.SolrHBaseUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CountDownLatch;

/**
 * Created by Administrator on 2015/12/10.
 */
public class MMM {
    public static void main(String[] args) throws Exception {
        MMM test = new MMM();
        test.testByteAdd();
    }

    public static String toSha1Value(String uri) {
        if (uri == null) {
            return null;
        }
        //use lower case url for unique.
        uri = uri.toLowerCase();
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA1");
            messageDigest.update(uri.getBytes("UTF-8"));

            StringBuilder hexString = new StringBuilder();
            byte[] hash = messageDigest.digest();

            for (byte aHash : hash) {
                if ((0xff & aHash) < 0x10) {
                    hexString.append("0").append(Integer.toHexString((0xFF & aHash)));
                } else {
                    hexString.append(Integer.toHexString(0xFF & aHash));
                }
            }
            return hexString.toString();
        } catch (Exception e) {

        }

        return null;
    }

    private static final Logger logger = LoggerFactory.getLogger(MMM.class);

    public void testByteAdd() throws Exception {
        String id = "10cf36689d66d37d19db4d583c2b1f6e49a8f6e3";
        RowKeyGenerator keyGenerator = SolrHBaseUtils.rowKeyGenerator(100);

        Random random = new Random();
        HBaseSolrData solrData = SolrHBaseUtils.get("table_test_1", null);
        CountDownLatch latch = new CountDownLatch(20);
        for (int k = 0; k < 20; k++) {
            new Thread(() -> {
                for (int l = 0; l < 1000; l++) {
                    Map<byte[], Map<String, Object>> inputDataMap = new HashMap<>();
                    for (int i = 0; i < 1000; i++) {
                        Map<String, Object> d = new HashMap<>();
                        for (int j = 0; j < 10; j++) {
                            d.put("field" + j, "value" + System.currentTimeMillis() + j);
                        }
                        //String _id = "row" + random.nextInt(100000);
                        String _id = toSha1Value("row" + System.currentTimeMillis() + random.nextInt(10000));
                        inputDataMap.put(keyGenerator.rowKey(_id), d);
                    }
                    try {
                        solrData.insertData(inputDataMap);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    logger.info("{}", l);
                }
                latch.countDown();
            }).start();

        }


        byte[] b = keyGenerator.rowKey(id);
        String i = keyGenerator.rowKey(b);


        System.out.println();

        latch.await();
    }

    static {
        System.setProperty("table.prefix", "solr_core_");
        System.setProperty("table.suffix", "_data");
    }
}
