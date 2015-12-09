package me.robin.mail;

import junit.framework.TestCase;
import me.robin.hbase.CollectionStore;
import me.robin.hbase.Configure;
import me.robin.hbase.HBaseTableOP;
import me.robin.solr.util.HBaseSolrData;
import me.robin.solr.util.RowKeyGenerator;
import me.robin.solr.util.SolrHBaseUtils;
import org.apache.hadoop.hbase.util.Bytes;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import org.jsoup.parser.XmlTreeBuilder;

import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Created by Administrator on 2015/10/27.
 */
public class ATest extends TestCase {
    public static void main(String[] args) {

        String json = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<Pensons>\n" +
                "\t<Penson id=\"1\" city=\"zj\">\n" +
                "\t\t<name>name</name>\n" +
                "\t\t<sex>男</sex>\n" +
                "\t\t<age>30</age>\n" +
                "\t</Penson>\n" +
                "\t<Penson id=\"2\" city=\"zj\">\n" +
                "\t\t<name>name</name>\n" +
                "\t\t<sex>男</sex>\n" +
                "\t\t<age>30</age>\n" +
                "\t</Penson>\n" +
                "\t<Penson id=\"4\" city=\"zj\">\n" +
                "\t\t<name>name</name>\n" +
                "\t\t<sex>男</sex>\n" +
                "\t\t<age>30</age>\n" +
                "\t</Penson>\n" +
                "</Pensons>";

        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<root>\n" +
                " <head id=\"common\">\n" +
                " <item key=\"Host\">www.nbcredit.net</item>\n" +
                " <item key=\"Connection\">Keep-Alive</item>\n" +
                " <item key=\"Accept\">text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8</item>\n" +
                " <item key=\"Proxy-Connection\">Keep-Alive</item>\n" +
                " <item key=\"Accept-Language\">zh-cn,zh;q=0.8,en-us;q=0.5,en;q=0.3</item>\n" +
                " <item key=\"Accept-Encoding\">gzip,deflate</item>\n" +
                " </head>\n" +
                " <head id=\"json\">\n" +
                " <item key=\"Accept\">application/json, text/javascript, */*</item>\n" +
                " </head>\n" +
                "</root>";

        Document document = Jsoup.parse(json);


        Document xmlDoc = Jsoup.parse(xml, "", new Parser(new XmlTreeBuilder()));

        System.out.println();
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

    public void testByteAdd() throws Exception {
        String id = "10cf36689d66d37d19db4d583c2b1f6e49a8f6e3";
        RowKeyGenerator keyGenerator = SolrHBaseUtils.rowKeyGenerator(100);
        Map<byte[], Map<String, Object>> inputDataMap = new HashMap<>();
        Random random = new Random();
        for (int i = 0; i < 100000; i++) {
            Map<String, Object> d = new HashMap<>();
            for (int j = 0; j < 10; j++) {
                d.put("field" + j, "value" + j);
            }
            //String _id = "row" + random.nextInt(100000);
            String _id = toSha1Value("row" + random.nextInt(10000));
            inputDataMap.put(keyGenerator.rowKey(_id),d);
        }


        byte[] b = keyGenerator.rowKey(id);
        String i = keyGenerator.rowKey(b);
        HBaseSolrData solrData = SolrHBaseUtils.get("table_test_1", null);
        solrData.insertData(inputDataMap);
        System.out.println();
    }

    static {
        System.setProperty("table.prefix", "solr_core_");
        System.setProperty("table.suffix", "_data");
    }

    public void testCreateTable() throws Exception {
        HBaseTableOP.deleteTable(Configure.getConfiguration(),"solr_core_table_test_1_data");
        CollectionStore.initialize("table_test_1", 100);
    }
}
