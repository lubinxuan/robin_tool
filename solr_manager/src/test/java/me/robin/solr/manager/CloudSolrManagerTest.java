package me.robin.solr.manager;

import me.robin.solr.shard.MyImplicitDocRouter;
import me.robin.solr.shard.ShardRouter;
import org.apache.commons.lang3.StringUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CloudSolrClient;
import org.apache.solr.client.solrj.request.QueryRequest;
import org.apache.solr.common.util.NamedList;
import org.apache.zookeeper.KeeperException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.UnsupportedEncodingException;


/**
 * Created by Lubin.Xuan on 2016/2/22.
 */
public class CloudSolrManagerTest {
    CloudSolrClient cloudSolrServer;

    String port = "3181";

    @Before
    public void setUp() throws InterruptedException, UnsupportedEncodingException, KeeperException, NoSuchFieldException, IllegalAccessException {
        //HttpClientUtil.addRequestInterceptor(new TestHttpRequestInterceptor());
        cloudSolrServer = new CloudSolrClient("172.16.2.30:" + port + ",172.16.2.31:" + port + ",172.16.2.32:" + port);
        cloudSolrServer.setZkClientTimeout(30000);
        cloudSolrServer.setZkConnectTimeout(30000);
        cloudSolrServer.connect();
        MyImplicitDocRouter.init(cloudSolrServer, new ShardRouter.ShardRouterKeyParser() {

            private String routeField = "crawltime";

            @Override
            public String[] parse(String collection, String q, String fq) {

                String[] timeRange = new String[]{null, null};

                if (null != q && q.contains(routeField)) {
                    String val = MyImplicitDocRouter.queryValue(routeField, fq);
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
    }

    @After
    public void destroy() throws IOException {
        cloudSolrServer.close();
    }


    @Test
    public void create2016_3() throws InterruptedException, SolrServerException, KeeperException, IOException {
        CloudSolrManager solrManager = new CloudSolrManager("admonitor", cloudSolrServer);
        solrManager.createShard("2016_4", "2016-04-01", "2016-05-01");
    }


    @Test
    public void querySolr() throws IOException, SolrServerException {
        SolrQuery query = new SolrQuery();
        query.setQuery("crawltime:[2015-12-01T23:21:32Z TO 2016-12-01T23:21:32Z] AND source:news");
        query.addFilterQuery("crawltime:[2015-12-10T23:21:32Z TO 2016-02-01T23:21:32Z] AND title:\"诸暨\"");
        QueryRequest request = new QueryRequest(query, SolrRequest.METHOD.GET);
        NamedList val = cloudSolrServer.request(request, "admonitor");
        System.out.println(val);
    }
}