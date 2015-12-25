import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CloudSolrClient;
import org.apache.solr.client.solrj.request.CollectionAdminRequest;

import java.io.IOException;

/**
 * Created by Lubin.Xuan on 2015/12/25.
 */
public class CollectionOp {
    public static void main(String[] args) throws IOException, SolrServerException {
        CloudSolrClient cloudSolrServer = new CloudSolrClient("172.16.2.30:3181,172.16.2.31:3181,172.16.2.32:3181");
        cloudSolrServer.setZkClientTimeout(30000);
        cloudSolrServer.setZkConnectTimeout(30000);
        cloudSolrServer.connect();

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
        create.setShards("2013_2014,2015_1_6,2015_7_12,2016_1_3");
        cloudSolrServer.request(create);
        cloudSolrServer.close();
    }
}
