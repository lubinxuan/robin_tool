package me.robin.solr;

import me.robin.solr.shard.MyImplicitDocRouter;
import me.robin.solr.shard.ShardRouter;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.HttpClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.*;
import org.apache.solr.common.cloud.Slice;
import org.apache.solr.common.params.MapSolrParams;
import org.apache.solr.common.params.SolrParams;
import org.apache.zookeeper.KeeperException;
import org.testng.annotations.BeforeTest;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

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


        MyImplicitDocRouter.ServerSelector serverSelector = new MyImplicitDocRouter.ServerSelector() {

            class NodeRef {
                private String node;
                private long queryRef;

                public NodeRef(String node, long queryRef) {
                    this.node = node;
                    this.queryRef = queryRef;
                }

                public String getNode() {
                    return node;
                }

                public long getQueryRef() {
                    return queryRef;
                }

                public void addRef() {
                    this.queryRef += 1;
                }

                @Override
                public boolean equals(Object o) {
                    if (this == o) return true;
                    if (o == null || getClass() != o.getClass()) return false;

                    NodeRef nodeRef = (NodeRef) o;

                    return node.equals(nodeRef.node);

                }

                @Override
                public int hashCode() {
                    return node.hashCode();
                }
            }

            private Map<String, String> nodeCache = new ConcurrentHashMap<>();
            private Set<NodeRef> nodeRefSet = new HashSet<>();


            @Override
            public synchronized List<String> select(List<String> servers) {

                Collection<Slice> slices = MyImplicitDocRouter.currentSlices();



                String nodeSelect;
                Long preCount;
                for (String server : servers) {
                    String node = node(server);
                }
                return null;
            }

            private String node(String server) {
                return nodeCache.compute(server, (k, s2) -> {
                    if (null != s2) {
                        return s2;
                    }
                    int s = server.indexOf("//");
                    int e = server.indexOf("/", s + 2);
                    return server.substring(s + 2, e + 1);
                });
            }
        };


        LBHttpSolrClient lbClient = new LBHttpSolrClient(client) {
            @Override
            public Rsp request(Req req) throws SolrServerException, IOException {
                /*int idx = req.getServers().indexOf("http://172.16.2.17:8888/solr");
                boolean singleShardsQuery = req.getRequest().getParams().getBool(MyImplicitDocRouter.SINGLE_SHARDS_QUERY, false);
                if (singleShardsQuery) {

                }*/
                for (Iterator<String> iterator = req.getServers().iterator(); iterator.hasNext(); ) {
                    String server = iterator.next();
                    if (!server.contains("172.16.2.17:8888")) {
                        iterator.remove();
                    }
                }
                    /*if (idx > -1) {
                        req.getServers().remove(idx);
                    }*/
                return super.request(req);
            }
        };
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
