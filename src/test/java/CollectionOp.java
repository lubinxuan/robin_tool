import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CloudSolrClient;
import org.apache.solr.client.solrj.request.CollectionAdminRequest;
import org.apache.solr.client.solrj.request.UpdateRequest;
import org.apache.solr.client.solrj.request.schema.SchemaRequest;
import org.apache.solr.common.cloud.DocCollection;
import org.apache.solr.common.cloud.ZkStateReader;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.params.UpdateParams;
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

    String port = "3181";

    @Before
    public void setUp() {
        cloudSolrServer = new CloudSolrClient("172.16.2.30:3181");
        cloudSolrServer.setZkClientTimeout(30000);
        cloudSolrServer.setZkConnectTimeout(30000);
        cloudSolrServer.connect();
    }

    @After
    public void destroy() throws IOException {
        cloudSolrServer.close();
    }


    @Test
    public void addReplica() throws IOException, SolrServerException {
        CollectionAdminRequest.AddReplica addReplica = new CollectionAdminRequest.AddReplica();
        addReplica.setNode("172.16.2.30:8891_solr");
        addReplica.setShardName("2016_1_3");
        addReplica.setCollectionName("weibo");
        cloudSolrServer.request(addReplica);
    }


    @Test
    public void deleteData() throws IOException, SolrServerException {
        UpdateRequest updateRequest = new UpdateRequest();
        updateRequest.setParam(UpdateParams.COLLECTION, "video");
        updateRequest.deleteByQuery("*:*");
        cloudSolrServer.request(updateRequest);
    }

    @Test
    public void test() throws IOException, SolrServerException {

        ZkStateReader stateReader = cloudSolrServer.getZkStateReader();
        DocCollection collection = ZkStateReader.getCollectionLive(stateReader,"admonitor_test");

        System.out.println();


       /* CollectionAdminRequest.Delete delete = new CollectionAdminRequest.Delete();
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
        cloudSolrServer.request(create);*/

    }


    @Test
    public void wbUSer() throws IOException, SolrServerException {

        CollectionAdminRequest.Create create = new CollectionAdminRequest.Create();
        create.setMaxShardsPerNode(2);
        create.setNumShards(3);
        create.setCollectionName("test_col");
        create.setConfigName("secret");
        cloudSolrServer.request(create);

    }


}
