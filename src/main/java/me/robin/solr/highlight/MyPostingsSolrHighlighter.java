package me.robin.solr.highlight;

import org.apache.lucene.search.postingshighlight.PostingsHighlighter;
import org.apache.solr.highlight.MySolrExtendedPostingsHighlighter;
import org.apache.solr.highlight.PostingsSolrHighlighter;
import org.apache.solr.request.SolrQueryRequest;

/**
 * Created by Lubin.Xuan on 2015/10/20.
 * ie.
 */
public class MyPostingsSolrHighlighter extends PostingsSolrHighlighter {

    @Override
    protected PostingsHighlighter getHighlighter(SolrQueryRequest req) {
        return new MySolrExtendedPostingsHighlighter(req, this);
    }
}
