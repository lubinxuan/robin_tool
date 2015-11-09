package me.robin.solr.highlight;

import me.robin.solr.util.HBaseDataThreadUtil;
import org.apache.lucene.document.Document;
import org.apache.solr.core.SolrCore;
import org.apache.solr.highlight.DefaultSolrHighlighter;
import org.apache.solr.request.SolrQueryRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Created by Lubin.Xuan on 2015/10/19.
 * ie.
 */
public class MyDefaultSolrHighlighter extends DefaultSolrHighlighter {

    private static final Logger logger = LoggerFactory.getLogger(MyDefaultSolrHighlighter.class);

    public MyDefaultSolrHighlighter(SolrCore solrCore) {
        super(solrCore);
    }

    @Override
    protected List<String> getFieldValues(Document doc, String fieldName, int maxValues, int maxCharsToAnalyze, SolrQueryRequest req) {
        List<String> result = super.getFieldValues(doc, fieldName, maxValues, maxCharsToAnalyze, req);
        if (result.isEmpty()) {
            Map<String, Object> data = HBaseDataThreadUtil.getDoc(doc.get(req.getSchema().getUniqueKeyField().getName()));
            if (null != data && data.containsKey(fieldName)) {
                Object val = data.get(fieldName);
                if (val instanceof String) {
                    result.add((String) val);
                } else if (null != val) {
                    if (val.getClass().isArray() && String.class.equals(val.getClass().getComponentType())) {
                        String[] arr = (String[]) val;
                        result.addAll(Arrays.asList(arr));
                    } else if (val instanceof List) {
                        List list = (List) val;
                        for (Object o : list) {
                            result.add(String.valueOf(o));
                        }
                    }
                }
            }
            logger.info("{} {} {}", fieldName, result, data);
        } else {
            logger.info("{} {}", fieldName, result);
        }
        return result;
    }
}
