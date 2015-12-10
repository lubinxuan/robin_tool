import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import jodd.http.HttpRequest;
import me.robin.solr.util.HBaseMapper;
import me.robin.solr.util.HBaseSolrData;
import me.robin.solr.util.SolrHBaseUtils;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.lucene.util.BytesRef;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2015/12/10.
 */
public class NNN {

    static {
        System.setProperty("table.prefix", "");
        System.setProperty("table.suffix", "_solr_store_data");
    }

    public static void main(String[] args) {
        HttpRequest request = HttpRequest.get("http://172.16.2.30:8983/solr/admonitor_shard1_replica1/select?q=*%3A*&rows=50000&fl=docid&wt=json&indent=true");
        JSONArray array = JSON.parseObject(request.send().bodyText()).getJSONObject("response").getJSONArray("docs");
        List<byte[]> bytes = new ArrayList<>();
        for (int i=0;i<array.size();i++){
            bytes.add(Bytes.toBytes(array.getJSONObject(i).getString("docid")));
        }
        /*Map<String,Map<String,BytesRef>> dataMap = solrData.getByRowKeyColl(bytes, new HBaseMapper<Map<String, BytesRef>>() {
            @Override
            public HBaseSolrData.Entry<Map<String, BytesRef>> mapper(byte[] rowKey, Map<String, BytesRef> dataMap) {
                return new HBaseSolrData.Entry<Map<String, BytesRef>>(Bytes.toString(rowKey),dataMap);
            }
        });*/
        HBaseSolrData solrData = SolrHBaseUtils.get("admonitor", null);
        Map<String,Map<String,Object>> dataMap =solrData.getByRowKeyColl(bytes, new HBaseMapper<Map<String, Object>>() {
            @Override
            public HBaseSolrData.Entry<Map<String, Object>> mapper(byte[] rowKey, Map<String, BytesRef> dataMap) {
                Map<String,Object> mapMap = new HashMap<String, Object>();
                mapMap.put("data",dataMap);
                return new HBaseSolrData.Entry<Map<String, Object>>(Bytes.toString(rowKey),mapMap);
            }
        });

        System.out.println();
    }
}
