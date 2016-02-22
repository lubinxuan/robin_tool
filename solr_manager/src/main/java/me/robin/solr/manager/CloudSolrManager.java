package me.robin.solr.manager;

import me.robin.solr.shard.ShardConfigHelper;
import me.robin.solr.shard.ShardRouter;
import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CloudSolrClient;
import org.apache.solr.client.solrj.request.CollectionAdminRequest;
import org.apache.solr.common.cloud.DocCollection;
import org.apache.solr.common.cloud.Replica;
import org.apache.solr.common.cloud.Slice;
import org.apache.solr.common.cloud.ZkStateReader;
import org.apache.solr.common.util.NamedList;
import org.apache.zookeeper.KeeperException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;

/**
 * Created by Lubin.Xuan on 2016/2/22.
 */
public class CloudSolrManager {

    private static final Logger logger = LoggerFactory.getLogger(CloudSolrManager.class);

    private final String collection;
    private final CloudSolrClient cloudSolrServer;
    private final ShardConfigHelper helper;
    private final ShardRouter.ShardReader shardReader;
    private Map<String, Set<String>> serverLoad = new HashMap<>();
    private Set<String> liveNodes = new HashSet<>();

    public CloudSolrManager(String collection, CloudSolrClient cloudSolrServer) {
        this.collection = collection;
        this.cloudSolrServer = cloudSolrServer;
        this.helper = new ShardConfigHelper(cloudSolrServer.getZkStateReader().getZkClient().getSolrZooKeeper());
        this.shardReader = new ShardRouter.ShardReader(cloudSolrServer.getZkStateReader().getZkClient().getSolrZooKeeper());
    }

    public void createShard(String shardName, String routeStart, String routeEnd) throws IOException, SolrServerException, KeeperException, InterruptedException {
        CollectionAdminRequest.CreateShard request = new CollectionAdminRequest.CreateShard();
        request.setCollectionName(collection);
        request.setShardName(shardName);
        //todo select node
        request.setNodeSet(selectLowerLoadServer());
        //todo add route define on solr
        request(request);
        helper.addShardConfig(collection, shardName, routeStart, routeEnd);
    }

    public void addReplica(String shardName) throws IOException, SolrServerException {
        CollectionAdminRequest.AddReplica request = new CollectionAdminRequest.AddReplica();
        request.setCollectionName(collection);
        request.setShardName(shardName);
        request.setNode(selectLowerLoadServer());
        request(request);
    }

    public void dropReplica(String shardName, String replicaName) throws IOException, SolrServerException {
        CollectionAdminRequest.DeleteReplica request = new CollectionAdminRequest.DeleteReplica();
        request.setCollectionName(collection);
        request.setShardName(shardName);
        request.setReplica(replicaName);
        request(request);
    }

    private void request(SolrRequest request) throws IOException, SolrServerException {
        NamedList<Object> rsp = cloudSolrServer.request(request);

        printOpResult(rsp);
    }

    private void printOpResult(NamedList<Object> rsp) {
        logger.debug("{}", rsp);
    }


    private void updateCloudInfo() {

        this.liveNodes = cloudSolrServer.getZkStateReader().getClusterState().getLiveNodes();
        if (this.liveNodes.isEmpty()) {
            throw new IllegalArgumentException("没有发现可用服务实例!!!");
        }

        DocCollection docCollection = ZkStateReader.getCollectionLive(cloudSolrServer.getZkStateReader(), collection);
        Map<String, Slice> sliceMap = docCollection.getActiveSlicesMap();
        Map<String, Set<String>> serverLoad = new HashMap<>();
        for (Map.Entry<String, Slice> entry : sliceMap.entrySet()) {
            String shard = entry.getKey();
            Slice slice = entry.getValue();
            for (Map.Entry<String, Replica> e : slice.getReplicasMap().entrySet()) {
                String name = e.getKey();
                Replica replica = e.getValue();
                Map<String, Object> propMap = replica.getProperties();
                String coreName = String.valueOf(propMap.get("core"));
                String nodeName = String.valueOf(propMap.get("node_name"));
                serverLoad.compute(nodeName, new BiFunction<String, Set<String>, Set<String>>() {
                    @Override
                    public Set<String> apply(String s, Set<String> strings) {
                        if (null == strings) {
                            strings = new HashSet<String>();
                        }
                        strings.add(coreName);
                        return strings;
                    }
                });
            }
        }
        this.serverLoad = serverLoad;
    }

    private String selectLowerLoadServer() {
        updateCloudInfo();
        String _server = null;
        int serverLoadReplica = -1;
        for (String server : liveNodes) {
            if (!serverLoad.containsKey(server)) {
                return server;
            }
            Set<String> replica = serverLoad.get(server);
            if (null == _server) {
                _server = server;
                serverLoadReplica = replica.size();
            }

            if (serverLoadReplica > replica.size()) {
                _server = server;
                serverLoadReplica = replica.size();
            }
        }
        return _server;
    }

}
