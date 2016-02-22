package me.robin.solr.manager;

import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CloudSolrClient;
import org.apache.zookeeper.KeeperException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;


/**
 * Created by Lubin.Xuan on 2016/2/22.
 */
public class CloudSolrManagerTest {
    CloudSolrClient cloudSolrServer;

    String port = "3181";

    @Before
    public void setUp() {
        cloudSolrServer = new CloudSolrClient("172.16.2.30:" + port + ",172.16.2.31:" + port + ",172.16.2.32:" + port);
        cloudSolrServer.setZkClientTimeout(30000);
        cloudSolrServer.setZkConnectTimeout(30000);
        cloudSolrServer.connect();
    }

    @After
    public void destroy() throws IOException {
        cloudSolrServer.close();
    }


    @Test
    public void create2016_3() throws InterruptedException, SolrServerException, KeeperException, IOException {
        CloudSolrManager solrManager = new CloudSolrManager("admonitor", cloudSolrServer);
        solrManager.createShard("2016_4","2016-04-01","2016-05-01");
    }
}