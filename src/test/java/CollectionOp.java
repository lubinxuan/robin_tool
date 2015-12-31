import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CloudSolrClient;
import org.apache.solr.client.solrj.request.CollectionAdminRequest;
import org.apache.solr.client.solrj.request.schema.SchemaRequest;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.apache.solr.common.params.SolrParams;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Lubin.Xuan on 2015/12/25.
 */
public class CollectionOp {

    CloudSolrClient cloudSolrServer;

    @Before
    public void setUp() {
        cloudSolrServer = new CloudSolrClient("172.16.2.30:3181,172.16.2.31:3181,172.16.2.32:3181");
        cloudSolrServer.setZkClientTimeout(30000);
        cloudSolrServer.setZkConnectTimeout(30000);
        cloudSolrServer.connect();
    }

    @After
    public void destroy() throws IOException {
        cloudSolrServer.close();
    }


    @Test
    public void addShard() throws IOException, SolrServerException {
        CollectionAdminRequest.CreateShard shard = new CollectionAdminRequest.CreateShard();
        shard.setCollectionName("admonitor");
        shard.setShardName("2016_1");
        cloudSolrServer.request(shard);
    }

    @Test
    public void addField() throws IOException, SolrServerException {
    }

    @Test
    public void test() throws IOException, SolrServerException {


        CollectionAdminRequest.Delete delete = new CollectionAdminRequest.Delete();
        delete.setCollectionName("weibo");

        cloudSolrServer.request(delete);

        CollectionAdminRequest.Create create = new CollectionAdminRequest.Create();
        //http://172.16.2.30:8888/solr/admin/collections?
        // action=CREATE&
        // name=admonitor&
        // router.name=implicit&
        // shards=2013,2014_1_6,2014_7_10,2014_11_12,2015_1_3,2015_4_5,2015_6_7,2015_8,2015_9,2015_10,2015_11,2015_12&
        // maxShardsPerNode=3
        create.setMaxShardsPerNode(2);
        create.setCollectionName("weibo");
        create.setRouterName("implicit");
        create.setShards("2013_2014,2015_1_4,2015_5_12,2016_1_3");
        cloudSolrServer.request(create);

    }


    @Test
    public void wbUSer() throws IOException, SolrServerException {

        CollectionAdminRequest.Create create = new CollectionAdminRequest.Create();
        create.setMaxShardsPerNode(2);
        create.setNumShards(3);
        create.setCollectionName("wb_user");
        cloudSolrServer.request(create);

    }


}
