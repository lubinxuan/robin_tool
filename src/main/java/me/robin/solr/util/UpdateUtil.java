package me.robin.solr.util;

import org.apache.solr.common.params.SolrParams;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.update.processor.DistributedUpdateProcessor;

import static org.apache.solr.update.processor.DistributingUpdateProcessorFactory.DISTRIB_UPDATE_PARAM;

/**
 * Created by Lubin.Xuan on 2015/10/14.
 * ie.
 */
public class UpdateUtil {
    public static boolean isProcessRequired(SolrQueryRequest req) {
        SolrParams rp = req.getParams();
        String distributeUpdate = null;
        if (null != rp) {
            distributeUpdate = rp.get(DISTRIB_UPDATE_PARAM);
        }
        return !DistributedUpdateProcessor.DistribPhase.FROMLEADER.toString().equals(distributeUpdate) && !DistributedUpdateProcessor.DistribPhase.TOLEADER.toString().equals(distributeUpdate);
    }
}
