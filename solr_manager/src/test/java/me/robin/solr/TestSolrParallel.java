package me.robin.solr;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.client.solrj.impl.HttpClientUtil;
import org.apache.solr.client.solrj.request.QueryRequest;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.params.CommonParams;
import org.apache.solr.common.params.ShardParams;
import org.apache.solr.common.params.UpdateParams;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.common.util.SimpleOrderedMap;
import org.apache.zookeeper.KeeperException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterTest;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Lubin.Xuan on 2016/2/29.
 */
public class TestSolrParallel extends SolrCloudBase {

    private static final Logger logger = LoggerFactory.getLogger(TestSolrParallel.class);

    String[] words = new String[]{
            "财务", "税务", "会计", "审计", "最新", "政策", "制度", "优惠", "减免", "免费", "培训", "学习", "教育", "法规", "财务处理", "财务分析", "财务报告", "财务项目申报", "财务报告制度", "财务会计报告条例", "财务策划", "财务证书", "财务年报", "财务管理", "财务总结", "财经政策", "最新", "政策", "制度", "优惠", "减免", "免费", "培训", "学习", "教育", "法规", "三证合一", "登记制度", "行政审批", "最新", "政策", "制度", "优惠", "减免", "免费", "培训", "学习", "教育", "会计准则", "会计知识", "管理会计", "基础会计", "注册会计", "CPA制度", "注册会计", "会计准则", "会计改革", "会计信息化", "会计学", "会计制度", "会计法 ", "内部控制", "审计准则", "审计方法", "CPA审计", "内控规范"
    };

    Date date = new Date();


    public TestSolrParallel() throws InterruptedException, NoSuchFieldException, IllegalAccessException, KeeperException, UnsupportedEncodingException {
    }

    @AfterTest
    public void shutdown() throws IOException {
        Date end = new Date();
        System.out.println("start at :" + date + " stop at:" + end + "  cost:" + (end.getTime() - date.getTime()));
        super.destroy();
    }

    private synchronized String[] random() {
        Random random = new Random();
        Set<String> filter = new HashSet<>();
        String[] ws = new String[5];
        for (int i = 0; i < 5; i++) {
            int idx = random.nextInt(words.length);
            String w = words[idx];
            if (filter.add(w)) {
                ws[i] = w;
            }

        }
        return ws;
    }

    @Test(invocationCount = 100, threadPoolSize = 10, testName = "test-100-10-parallel")
    public void test10() throws Exception {
        testMethod();
    }

    @Test(invocationCount = 100, threadPoolSize = 20, testName = "test-100-20-parallel")
    public void test20() throws Exception {
        testMethod();
    }

    @Test(invocationCount = 10000, threadPoolSize = 30, testName = "test-30-parallel")
    public void test30() throws Exception {
        testMethod();
    }

    @Test(invocationCount = 10000, threadPoolSize = 50, testName = "test-50-parallel")
    public void test50() throws Exception {
        testMethod();
    }

    @Test(invocationCount = 10000, threadPoolSize = 70, testName = "test-70-parallel")
    public void test70() throws Exception {
        testMethod();
    }

    @Test(invocationCount = 10000, threadPoolSize = 90, testName = "test-90-parallel")
    public void test90() throws Exception {
        testMethod();
    }

    @Test(invocationCount = 10000, threadPoolSize = 100, testName = "test-100-parallel")
    public void test100() throws Exception {
        testMethod();
    }


    @Test(invocationCount = 10000, threadPoolSize = 200, testName = "test-200-parallel")
    public void test200() throws Exception {
        testMethod();
    }


    @Test(invocationCount = 10000, threadPoolSize = 500, testName = "test-500-parallel")
    public void test500() throws Exception {
        testMethod();
    }


    @Test(invocationCount = 10000, threadPoolSize = 800, testName = "test-800-parallel")
    public void test800() throws Exception {
        testMethod();
    }


    @Test(invocationCount = 10000, threadPoolSize = 1000, testName = "test-1000-parallel")
    public void test1000() throws Exception {
        testMethod();
    }

    Random random = new Random();

    final static boolean CONTENT, UPDATE;

    static {
        CONTENT = "true".equals(System.getProperty("solr.content"));
        UPDATE = "true".equals(System.getProperty("solr.increment"));
    }

    @Test(invocationCount = 10)
    private void testMethod() throws Exception {
        SolrQuery query = new SolrQuery();
        String[] ws = random();
        StringBuilder q = new StringBuilder();
        for (String w : ws) {
            if (q.length() > 0) {
                q.append(" OR ");
            }
            q.append("content:\"").append(w).append("\"");
        }

        query.setQuery(q.toString());
        query.addFilterQuery("crawltime:[2015-11-01T00:00:00Z TO 2015-11-30T23:23:59Z]");
        query.setParam(ShardParams.SHARDS, "172.16.2.17:8888/solr/admonitor");
        //query.setParam(CommonParams.DEBUG_QUERY, true);
        query.addSort("crawltime", SolrQuery.ORDER.desc);
        if (!CONTENT) {
            query.setFields("comment_count,author,crawltime,webname,click_count,source,title,seed_id,url,weburl,pubtime,region,docid");
        }
        //query.setFields("content");
        if (UPDATE) {
            query.setRows(100 + random.nextInt(500));
        } else {
            query.setRows(5000);
        }
        QueryRequest request = new QueryRequest(query, SolrRequest.METHOD.GET);
        query.setParam(UpdateParams.COLLECTION,"admonitor");
        long start = System.currentTimeMillis();
        NamedList val = cloudSolrServer.request(request);
        long numFound = ((SolrDocumentList) val.get("response")).getNumFound();
        Object qTime = ((SimpleOrderedMap) val.get("responseHeader")).get("QTime");
        long cost = System.currentTimeMillis() - start;
        //val.remove("response");
        System.out.println(Thread.currentThread().getName() + "   numFound:" + numFound + "   qTime:" + qTime + "   readCost:" + cost);
    }
}
