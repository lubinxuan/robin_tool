package me.robin.solr.handler.component;

import org.apache.lucene.search.Query;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.core.PluginInfo;
import org.apache.solr.core.SolrCore;
import org.apache.solr.handler.component.ResponseBuilder;
import org.apache.solr.highlight.DefaultSolrHighlighter;
import org.apache.solr.highlight.SolrHighlighter;

import java.io.IOException;
import java.util.List;

/**
 * Created by Lubin.Xuan on 2015/10/16.
 * ie.
 */
public class AfterHighlightComponent extends MyHighlightComponent {

    public static final String COMPONENT_NAME = "highlight_aft";

    @Override
    public void prepare(ResponseBuilder rb) throws IOException {

    }

    @Override
    public void process(ResponseBuilder rb) throws IOException {
        if (rb.doHighlights) {
            Query highlightQuery = rb.getHighlightQuery();
            String[] defaultHighlightFields = (String[]) rb.getDebugInfo().remove(DEFAULT_HIGH_LIGHT_FIELDS);
            // No highlighting if there is no query -- consider q.alt="*:*
            if (highlightQuery != null) {
                NamedList sumData = getHighlighter().doHighlighting(
                        rb.getResults().docList,
                        highlightQuery,
                        rb.req, defaultHighlightFields);

                if (sumData != null) {
                    // TODO ???? add this directly to the response?
                    rb.rsp.add("highlighting", sumData);
                }
            }
        }
    }
}
