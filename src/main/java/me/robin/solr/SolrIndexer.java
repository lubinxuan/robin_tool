package me.robin.solr;

import me.robin.solr.shard.ShardRouter;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.common.SolrInputDocument;

import java.util.List;

/**
 * Created by Lubin.Xuan on 2016/1/5.
 */
public class SolrIndexer {
    private final SolrClient solrClient;

    private final ShardRouter shardRouter;

    public SolrIndexer(SolrClient solrClient, ShardRouter shardRouter) {
        this.solrClient = solrClient;
        this.shardRouter = shardRouter;
    }

    public void index(String collection, List<SolrInputDocument> documentList) {
        if(null==documentList||documentList.isEmpty()){
            return;
        }
    }
}
