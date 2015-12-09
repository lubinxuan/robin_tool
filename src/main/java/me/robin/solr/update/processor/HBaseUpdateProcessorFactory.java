package me.robin.solr.update.processor;

import me.robin.solr.util.HBaseSolrData;
import me.robin.solr.util.SolrHBaseStore;
import me.robin.solr.util.SolrHBaseUtils;
import me.robin.solr.util.UpdateUtil;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.response.SolrQueryResponse;
import org.apache.solr.update.AddUpdateCommand;
import org.apache.solr.update.DeleteUpdateCommand;
import org.apache.solr.update.processor.UpdateRequestProcessor;
import org.apache.solr.update.processor.UpdateRequestProcessorFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Created by Lubin.Xuan on 2015/10/14.
 * ie.
 */
public class HBaseUpdateProcessorFactory extends UpdateRequestProcessorFactory implements UpdateRequestProcessorFactory.RunAlways {
    @Override
    public UpdateRequestProcessor getInstance(SolrQueryRequest req, SolrQueryResponse rsp, UpdateRequestProcessor next) {
        return new HBaseUpdateProcessor(next, req);
    }
}


class HBaseUpdateProcessor extends UpdateRequestProcessor {

    private static final Logger logger = LoggerFactory.getLogger(HBaseUpdateProcessor.class);

    private boolean processRequired;
    private SolrHBaseStore hBaseStore;
    private int adds = 0;
    private int dels = 0;

    public HBaseUpdateProcessor(UpdateRequestProcessor next, SolrQueryRequest req) {
        super(next);
        boolean processRequired = UpdateUtil.isProcessRequired(req);
        boolean hBaseStore = false;
        if (processRequired) {
            String core = SolrHBaseUtils.getCoreAliens(req.getCore().getName());
            hBaseStore = SolrHBaseUtils.isHBaseStoreCore(core);
            logger.debug("存储 Core:{} HBaseStore:{}", core, hBaseStore);
            if (hBaseStore) {
                HBaseSolrData hBaseSolrData = SolrHBaseUtils.get(core, req.getCore().getSolrConfig());
                if (null == hBaseSolrData) {
                    logger.warn("存储 Core:{} No HBaseSolrData initialize", core);
                    return;
                }
                this.hBaseStore = new SolrHBaseStore(req, hBaseSolrData);
            }
        }
        this.processRequired = processRequired && hBaseStore;
    }

    @Override
    public void processAdd(AddUpdateCommand cmd) throws IOException {
        if (processRequired) {
            logger.debug("solr doc add indexId:{}", cmd.getIndexedId());
            hBaseStore.add(cmd);
            adds++;
        }
        super.processAdd(cmd);
    }

    @Override
    public void processDelete(DeleteUpdateCommand cmd) throws IOException {
        if (processRequired) {
            if (cmd.isDeleteById()) {
                logger.debug("solr doc delete indexId:{}", cmd.getIndexedId());
                hBaseStore.delete(cmd);
            }
            //todo delete by query have no idea
            dels++;
        }
        super.processDelete(cmd);
    }

    @Override
    public void finish() throws IOException {
        if (processRequired) {
            try {
                hBaseStore.sendHBase();
            } catch (Throwable e) {
                throw new IOException(e.getCause());
            }
            logger.debug("process doc add:{}  del:{}", adds, dels);
        }
        super.finish();
    }

}