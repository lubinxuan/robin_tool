package me.robin.solr;

import me.robin.solr.shard.MyImplicitDocRouter;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.HttpClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.LBHttpSolrClient;
import org.apache.solr.common.cloud.Replica;
import org.apache.solr.common.cloud.Slice;
import org.apache.solr.common.params.CommonParams;
import org.apache.solr.common.params.ShardParams;
import org.apache.solr.common.params.SolrParams;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Created by Lubin.Xuan on 2016/3/11.
 * 对选定的分片节点的服务地址进行通过负载情况进行排序，优先查询负载低的节点
 * 说明，多节点情况下，负载不具备太多意义
 */
public class MyLBHttpSolrClient extends LBHttpSolrClient {

    public MyLBHttpSolrClient(HttpClient httpClient, String... solrServerUrl) {
        super(httpClient, solrServerUrl);
    }

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

    private Map<String, NodeRef> refMap = new ConcurrentHashMap<>();

    @Override
    public Rsp request(Req req) throws SolrServerException, IOException {
        SolrParams params = req.getRequest().getParams();
        if (params instanceof SolrQuery) {
            int serverSize = req.getServers().size();
            Collection<Slice> slices = MyImplicitDocRouter.currentSlices();
            SolrQuery query = (SolrQuery) params;
            if (serverSize > 1) {
                Map<String, Replica> sliceMap = new HashMap<>();
                for (Slice slice : slices) {
                    for (Replica replica : slice.getReplicas()) {
                        if (replica.getState() != Replica.State.ACTIVE) {
                            continue;
                        }
                        String coreUrl = replica.getCoreUrl();
                        sliceMap.put(coreUrl, replica);
                    }
                }


                for (int i = 0; i < serverSize - 1; i++) {
                    String cur = req.getServers().get(i);
                    for (int j = i + 1; j < serverSize; j++) {
                        String cmp = req.getServers().get(i);
                    }
                }
            }
            //如果只需要查询一个节点，设置分发操作为false，直连查询
            if (slices.size() == 1) {
                //直连查询
                query.setDistrib(false);
            } else {
                Set<String> shardNameSet = slices.stream().map(Slice::getName).collect(Collectors.toSet());
                query.setParam(ShardParams.SHARDS, StringUtils.join(shardNameSet, ","));
            }
        }
        return super.request(req);
    }


}
