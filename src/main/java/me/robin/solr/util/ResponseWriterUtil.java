package me.robin.solr.util;

/**
 * Created by Lubin.Xuan on 2015/10/16.
 * ie.
 */

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexableField;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.response.SolrQueryResponse;
import org.apache.solr.schema.IndexSchema;
import org.apache.solr.schema.SchemaField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ResponseWriterUtil {

    private static final Logger logger = LoggerFactory.getLogger(ResponseWriterUtil.class);

    /**
     * Utility method for converting a {@link Document} from the index into a
     * {@link SolrDocument} suitable for inclusion in a {@link SolrQueryResponse}
     */
    public static final SolrDocument toSolrDocument(Document doc, final IndexSchema schema) {
        SolrDocument out = new SolrDocument();
        String uniqueKey = doc.get(schema.getUniqueKeyField().getName());
        Map<String, Object> data = HBaseDataThreadUtil.getDoc(uniqueKey);
        logger.debug("{}  数据 doc hbase {} {}", Thread.currentThread(), uniqueKey, data);
        if (null == data || data.isEmpty()) {
            for (IndexableField f : doc.getFields()) {
                // Make sure multivalued fields are represented as lists
                Object existing = out.get(f.name());
                logger.debug("solr 值 {}", f);
                if (existing == null) {
                    SchemaField sf = schema.getFieldOrNull(f.name());
                    if (sf != null && sf.multiValued()) {
                        List<Object> vals = new ArrayList<>();
                        vals.add(f);
                        out.setField(f.name(), vals);
                    } else {
                        out.setField(f.name(), f);
                    }
                } else {
                    out.addField(f.name(), f);
                }
            }
        } else {
            Set<String> field = HBaseDataThreadUtil.field();
            for (String f : field) {
                SchemaField sf = schema.getFieldOrNull(f);
                if (null == sf) {
                    continue;
                }
                Object existing = out.get(f);
                IndexableField idf = doc.getField(f);
                logger.debug("数据 doc hbase {} {}", f, sf);
                if (null != idf) {
                    if (existing == null) {
                        if (sf.multiValued()) {
                            List<Object> vals = new ArrayList<>();
                            vals.add(f);
                            out.setField(f, vals);
                        } else {
                            out.setField(f, idf);
                        }
                    } else {
                        out.addField(f, idf);
                    }
                } else {
                    Object val = data.get(f);
                    if (null == val) {
                        continue;
                    }
                    out.setField(f, val);
                }
            }
        }
        return out;
    }
}