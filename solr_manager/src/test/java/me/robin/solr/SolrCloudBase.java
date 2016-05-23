package me.robin.solr;

import me.robin.solr.shard.MyImplicitDocRouter;
import me.robin.solr.shard.ShardRouter;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.HttpClient;
import org.apache.solr.client.solrj.impl.*;
import org.apache.solr.common.params.MapSolrParams;
import org.apache.solr.common.params.SolrParams;
import org.apache.zookeeper.KeeperException;
import org.testng.annotations.BeforeTest;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Lubin.Xuan on 2016/3/1.
 */
public class SolrCloudBase {
    protected CloudSolrClient cloudSolrServer;

    String port = "3181";

    public SolrCloudBase() throws InterruptedException, NoSuchFieldException, IllegalAccessException, KeeperException, UnsupportedEncodingException {
        cloudSolrServer = create(port);
    }


    public static CloudSolrClient create(String port) throws InterruptedException, UnsupportedEncodingException, IllegalAccessException, KeeperException, NoSuchFieldException {
        Map<String, String> configMap = new HashMap<>();
        configMap.put(HttpClientUtil.PROP_ALLOW_COMPRESSION, "true");
        SolrParams params = new MapSolrParams(configMap);
        HttpClient client = HttpClientUtil.createClient(params);


        LBHttpSolrClient lbClient = new MyLBHttpSolrClient(client);
        lbClient.setRequestWriter(new BinaryRequestWriter());
        lbClient.setParser(new BinaryResponseParser());


        CloudSolrClient cloudSolrServer = new CloudSolrClient("172.16.2.30:" + port + ",172.16.2.31:" + port + ",172.16.2.32:" + port, lbClient);
        cloudSolrServer.setZkClientTimeout(30000);
        cloudSolrServer.setZkConnectTimeout(30000);
        cloudSolrServer.connect();
        MyImplicitDocRouter.init(cloudSolrServer, new ShardRouter.ShardRouterKeyParser() {

            private String routeField = "crawltime";

            @Override
            public String[] parse(String collection, String q, String fq) {

                String[] timeRange = new String[]{null, null};

                if (null != q && q.contains(routeField)) {
                    String val = MyImplicitDocRouter.queryValue(routeField, q);
                    if (findTime(val, timeRange)) {
                        return timeRange;
                    }
                }

                if (null != fq && fq.contains(routeField)) {
                    String val = MyImplicitDocRouter.queryValue(routeField, fq);
                    findTime(val, timeRange);
                }

                return timeRange;
            }

            private boolean findTime(String val, String[] timeRange) {
                if (val.contains(" TO ")) {
                    String sp[] = val.substring(1, val.length() - 1).split(" TO ");
                    setRangeTime(timeRange, sp, 0);
                    setRangeTime(timeRange, sp, 1);
                } else {
                    timeRange[0] = timeRange[1] = val;
                }
                return StringUtils.equals(timeRange[0], timeRange[1]) && timeRange[0] != null;
            }

            private void setRangeTime(String[] timeRange, String[] sp, int idx) {
                if (!"*".equals(sp[idx])) {
                    if (StringUtils.isBlank(timeRange[idx]) || "*".equals(timeRange[idx])) {
                        timeRange[idx] = sp[idx];
                    } else {
                        if (idx == 0 && sp[idx].compareTo(timeRange[idx]) > 0) {
                            timeRange[idx] = sp[idx];
                        } else if (sp[idx].compareTo(timeRange[idx]) < 0) {
                            timeRange[idx] = sp[idx];
                        }
                    }
                }
            }
        });
        return cloudSolrServer;
    }

    @BeforeTest
    public void setUp() throws InterruptedException, UnsupportedEncodingException, KeeperException, NoSuchFieldException, IllegalAccessException {
        //HttpClientUtil.addRequestInterceptor(new TestHttpRequestInterceptor());
        cloudSolrServer = create(port);
    }

    public void destroy() throws IOException {
        cloudSolrServer.close();
    }

}
