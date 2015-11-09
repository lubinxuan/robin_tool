package org.apache.solr.response;

/**
 * Created by Lubin.Xuan on 2015/10/16.
 * ie.
 */

import org.apache.lucene.document.Document;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.schema.IndexSchema;

public class ResponseWriterUtil {

    /**
     * Utility method for converting a {@link Document} from the index into a
     * {@link SolrDocument} suitable for inclusion in a {@link SolrQueryResponse}
     */
    public static final SolrDocument toSolrDocument(Document doc, final IndexSchema schema) {
        return me.robin.solr.util.ResponseWriterUtil.toSolrDocument(doc, schema);
    }
}