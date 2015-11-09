package me.robin.solr.handler.component;

import com.google.common.base.Objects;
import org.apache.lucene.search.Query;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.params.CommonParams;
import org.apache.solr.common.params.HighlightParams;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.handler.component.ResponseBuilder;
import org.apache.solr.highlight.PostingsSolrHighlighter;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.search.QParser;
import org.apache.solr.search.QParserPlugin;
import org.apache.solr.search.QueryParsing;
import org.apache.solr.search.SyntaxError;

import java.io.IOException;

/**
 * Created by Lubin.Xuan on 2015/10/16.
 * ie.
 */
public class PreHighlightComponent extends MyHighlightComponent {

    public static final String COMPONENT_NAME = "highlight_pre";

    @Override
    public void prepare(ResponseBuilder rb) throws IOException {
        SolrParams params = rb.req.getParams();
        rb.doHighlights = getHighlighter().isHighlightingEnabled(params);
        if (rb.doHighlights) {
            String hlq = params.get(HighlightParams.Q);
            String hlparser = Objects.firstNonNull(params.get(HighlightParams.QPARSER),
                    params.get(QueryParsing.DEFTYPE, QParserPlugin.DEFAULT_QTYPE));
            if (hlq != null) {
                try {
                    QParser parser = QParser.getParser(hlq, hlparser, rb.req);
                    rb.setHighlightQuery(parser.getHighlightQuery());
                } catch (SyntaxError e) {
                    throw new SolrException(SolrException.ErrorCode.BAD_REQUEST, e);
                }
            }
        }
    }

    @Override
    public void process(ResponseBuilder rb) throws IOException {
        if (rb.doHighlights) {
            SolrQueryRequest req = rb.req;
            SolrParams params = req.getParams();

            String[] defaultHighlightFields;  //TODO: get from builder by default?

            if (rb.getQparser() != null) {
                defaultHighlightFields = rb.getQparser().getDefaultHighlightFields();
            } else {
                defaultHighlightFields = params.getParams(CommonParams.DF);
            }

            Query highlightQuery = rb.getHighlightQuery();
            if (highlightQuery == null) {
                if (rb.getQparser() != null) {
                    try {
                        highlightQuery = rb.getQparser().getHighlightQuery();
                        rb.setHighlightQuery(highlightQuery);
                    } catch (Exception e) {
                        throw new SolrException(SolrException.ErrorCode.BAD_REQUEST, e);
                    }
                } else {
                    highlightQuery = rb.getQuery();
                    rb.setHighlightQuery(highlightQuery);
                }
            }

            if (highlightQuery != null) {
                boolean rewrite = (!(getHighlighter() instanceof PostingsSolrHighlighter)) && !(Boolean.valueOf(params.get(HighlightParams.USE_PHRASE_HIGHLIGHTER, "true")) &&
                        Boolean.valueOf(params.get(HighlightParams.HIGHLIGHT_MULTI_TERM, "true")));
                highlightQuery = rewrite ? highlightQuery.rewrite(req.getSearcher().getIndexReader()) : highlightQuery;
                String[] highlight = getHighlighter().getHighlightFields(highlightQuery, rb.req, defaultHighlightFields);
                rb.getDebugInfo().add(HIGH_LIGHT_FIELDS, highlight);
            }

            rb.setHighlightQuery(highlightQuery);
            rb.getDebugInfo().add(DEFAULT_HIGH_LIGHT_FIELDS, defaultHighlightFields);

        }
    }
}
