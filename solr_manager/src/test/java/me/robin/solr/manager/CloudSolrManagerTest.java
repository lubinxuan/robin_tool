package me.robin.solr.manager;

import me.robin.solr.SolrCloudBase;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.zookeeper.KeeperException;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.UnsupportedEncodingException;


/**
 * Created by Lubin.Xuan on 2016/2/22.
 */
public class CloudSolrManagerTest extends SolrCloudBase {

    public CloudSolrManagerTest() throws InterruptedException, NoSuchFieldException, IllegalAccessException, KeeperException, UnsupportedEncodingException {
    }

    @Test
    public void create2016_3() throws InterruptedException, SolrServerException, KeeperException, IOException {
        CloudSolrManager solrManager = new CloudSolrManager("admonitor", cloudSolrServer);
        solrManager.createShard("2016_5", "2016-05-01", "2016-06-01");
    }


    @Test
    public void querySolr() throws IOException, SolrServerException {

    }
}