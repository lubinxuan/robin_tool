package org.apache.solr.highlight;

import me.robin.solr.util.HBaseDataThreadUtil;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.postingshighlight.*;
import org.apache.solr.common.params.HighlightParams;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.highlight.PostingsSolrHighlighter;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.schema.IndexSchema;

import java.io.IOException;
import java.text.BreakIterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;


public class MySolrExtendedPostingsHighlighter extends PostingsHighlighter {
    protected final SolrParams params;
    protected final IndexSchema schema;
    protected final PostingsSolrHighlighter highlighter;


    public MySolrExtendedPostingsHighlighter(SolrQueryRequest req, PostingsSolrHighlighter highlighter) {
        super(req.getParams().getInt(HighlightParams.MAX_CHARS, PostingsHighlighter.DEFAULT_MAX_LENGTH));
        this.params = req.getParams();
        this.schema = req.getSchema();
        this.highlighter = highlighter;
    }

    @Override
    protected Passage[] getEmptyHighlight(String fieldName, BreakIterator bi, int maxPassages) {
        boolean defaultSummary = params.getFieldBool(fieldName, HighlightParams.DEFAULT_SUMMARY, true);
        if (defaultSummary) {
            return super.getEmptyHighlight(fieldName, bi, maxPassages);
        } else {
            //TODO reuse logic of DefaultSolrHighlighter.alternateField
            return new Passage[0];
        }
    }

    @Override
    protected PassageFormatter getFormatter(String fieldName) {
        String preTag = params.getFieldParam(fieldName, HighlightParams.TAG_PRE, "<em>");
        String postTag = params.getFieldParam(fieldName, HighlightParams.TAG_POST, "</em>");
        String ellipsis = params.getFieldParam(fieldName, HighlightParams.TAG_ELLIPSIS, "... ");
        String encoder = params.getFieldParam(fieldName, HighlightParams.ENCODER, "simple");
        return new DefaultPassageFormatter(preTag, postTag, ellipsis, "html".equals(encoder));
    }

    @Override
    protected PassageScorer getScorer(String fieldName) {
        float k1 = params.getFieldFloat(fieldName, HighlightParams.SCORE_K1, 1.2f);
        float b = params.getFieldFloat(fieldName, HighlightParams.SCORE_B, 0.75f);
        float pivot = params.getFieldFloat(fieldName, HighlightParams.SCORE_PIVOT, 87f);
        return new PassageScorer(k1, b, pivot);
    }

    @Override
    protected BreakIterator getBreakIterator(String field) {
        String language = params.getFieldParam(field, HighlightParams.BS_LANGUAGE);
        String country = params.getFieldParam(field, HighlightParams.BS_COUNTRY);
        String variant = params.getFieldParam(field, HighlightParams.BS_VARIANT);
        Locale locale = highlighter.parseLocale(language, country, variant);
        String type = params.getFieldParam(field, HighlightParams.BS_TYPE);
        return highlighter.parseBreakIterator(type, locale);
    }

    @Override
    protected char getMultiValuedSeparator(String field) {
        String sep = params.getFieldParam(field, HighlightParams.MULTI_VALUED_SEPARATOR, " ");
        if (sep.length() != 1) {
            throw new IllegalArgumentException(HighlightParams.MULTI_VALUED_SEPARATOR + " must be exactly one character.");
        }
        return sep.charAt(0);
    }

    @Override
    protected Analyzer getIndexAnalyzer(String field) {
        if (params.getFieldBool(field, HighlightParams.HIGHLIGHT_MULTI_TERM, false)) {
            return schema.getIndexAnalyzer();
        } else {
            return null;
        }
    }

    @Override
    protected String[][] loadFieldValues(IndexSearcher searcher, String[] fields, int[] docids, int maxLength) throws IOException {
        /*String contents[][] = new String[fields.length][docids.length];
        char valueSeparators[] = new char[fields.length];
        for (int i = 0; i < fields.length; i++) {
            valueSeparators[i] = getMultiValuedSeparator(fields[i]);
        }
        LimitedStoredFieldVisitor visitor = new LimitedStoredFieldVisitor(fields, valueSeparators, maxLength);
        for (int i = 0; i < docids.length; i++) {
            searcher.doc(docids[i], visitor);
            for (int j = 0; j < fields.length; j++) {
                contents[j][i] = visitor.getValue(j).toString();
            }
            visitor.reset();
        }
        return contents;*/
        String contents[][] = super.loadFieldValues(searcher, fields, docids, maxLength);
        char valueSeparators[] = new char[fields.length];
        for (int i = 0; i < fields.length; i++) {
            valueSeparators[i] = getMultiValuedSeparator(fields[i]);
        }
        for (int i = 0; i < docids.length; i++) {
            Map<String, Object> data = HBaseDataThreadUtil.getDoc(docids[i]);
            if (null == data || data.isEmpty()) {
                continue;
            }
            for (int j = 0; j < fields.length; j++) {
                if (null == contents[j][i]) {
                    Object val = data.get(fields[j]);
                    if (val instanceof String) {
                        contents[j][i] = (String) val;
                    } else if (null != val) {
                        if (val.getClass().isArray() && String.class.equals(val.getClass().getComponentType())) {
                            String[] arr = (String[]) val;
                            contents[j][i] = String.join(valueSeparators[j] + "", arr);
                        } else if (val instanceof List) {
                            List arr = (List) val;
                            StringBuilder sb = new StringBuilder();
                            for (Object o : arr) {
                                if (sb.length() > 0) {
                                    sb.append(valueSeparators[j]);
                                }
                                sb.append(o);
                            }
                            contents[j][i] = sb.toString();
                        }
                    }
                }
            }
        }
        return contents;
    }
}