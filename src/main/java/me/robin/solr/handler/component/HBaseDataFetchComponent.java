package me.robin.solr.handler.component;

import me.robin.hbase.CollectionStore;
import me.robin.hbase.HBaseSolrData;
import me.robin.solr.util.HBaseDataThreadUtil;
import me.robin.solr.util.SolrHBaseUtils;
import me.robin.solr.util.SolrSchemeUtil;
import org.apache.lucene.document.Document;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.core.SolrCore;
import org.apache.solr.handler.component.ResponseBuilder;
import org.apache.solr.handler.component.SearchComponent;
import org.apache.solr.handler.component.ShardRequest;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.response.ResultContext;
import org.apache.solr.response.SolrQueryResponse;
import org.apache.solr.schema.IndexSchema;
import org.apache.solr.schema.SchemaField;
import org.apache.solr.search.DocIterator;
import org.apache.solr.search.DocList;
import org.apache.solr.search.ReturnFields;
import org.apache.solr.search.SolrIndexSearcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Lubin.Xuan on 2015/10/14.
 * ie.
 */
public class HBaseDataFetchComponent extends SearchComponent {

    public static final String COMPONENT_NAME = "hbase_data_fetch";

    private static final Logger logger = LoggerFactory.getLogger(HBaseDataFetchComponent.class);

    private static final String CORE_NAME = "coreName";

    boolean process = false;

    @Override
    public void prepare(ResponseBuilder rb) throws IOException {
        SolrQueryRequest req = rb.req;
        IndexSchema indexSchema = req.getSchema();
        SolrCore core = req.getCore();
        String coreName = SolrHBaseUtils.getCoreAliens(core.getName());
        logger.info("req info {}", req);
        Integer shardPurpose = req.getParams().getInt("shards.purpose");
        rb.addDebugInfo(CORE_NAME, coreName);
        if (null != shardPurpose) {
            if ((shardPurpose & ShardRequest.PURPOSE_GET_FIELDS) != 0
                    || (shardPurpose & ShardRequest.PURPOSE_GET_FIELDS & ShardRequest.PURPOSE_GET_HIGHLIGHTS) != 0
                    || (shardPurpose & ShardRequest.PURPOSE_GET_FIELDS & ShardRequest.PURPOSE_GET_HIGHLIGHTS & ShardRequest.PURPOSE_GET_DEBUG) != 0)
                process = true;
        } else {
            return;
        }

        SolrSchemeUtil.schemeInfo(coreName, indexSchema);
    }

    @Override
    public void process(ResponseBuilder rb) throws IOException {

        if (!process) {
            return;
        }

        SolrQueryResponse rsp = rb.rsp;
        NamedList namedList = rsp.getValues();

        if (null != namedList) {
            Object response = namedList.get("response");
            if (response instanceof ResultContext) {
                ResultContext _response = (ResultContext) response;
                if (null != _response.docs) {
                    loadData(_response.docs, rb);
                }
            }
        }

    }


    /**
     * 从HBase 获取数据
     *
     * @param docList
     * @param rb
     * @throws IOException
     */
    private void loadData(DocList docList, ResponseBuilder rb) throws IOException {

        if (docList.size() < 1) {
            return;
        }

        String coreName = (String) rb.getDebugInfo().get(CORE_NAME);
        SolrQueryRequest solrReq = rb.req;
        SolrQueryResponse solrRsp = rb.rsp;
        if (!SolrHBaseUtils.isHBaseStoreCore(coreName)) {
            return;
        }

        IndexSchema indexSchema = solrReq.getSchema();
        ReturnFields returnFields = solrRsp.getReturnFields();

        long start = System.currentTimeMillis();
        HBaseSolrData hBaseSolrData = CollectionStore.get(coreName);

        if (null == hBaseSolrData) {
            return;
        }

        logger.info("开始HBase 获取数据 ......{} {}", coreName, solrReq.getSearcher().getCore().getName());

        boolean isAllField = false;

        SchemaField uniqueKey = indexSchema.getUniqueKeyField();

        final Map<String, SchemaField> schemaFieldMap = SolrSchemeUtil.schemeInfo(coreName);
        Collection<SchemaField> fieldRequired = schemaFieldMap.values();
        if (null == returnFields || returnFields.wantsAllFields()) {
            isAllField = true;
        } else {
            fieldRequired = fieldRequired.stream().filter(sf -> returnFields.getRequestedFieldNames().contains(sf.getName())).collect(Collectors.toList());
        }

        Set<String> filedFilter = solrFieldWant(fieldRequired);
        Set<String> solrFilter = new HashSet<>();
        solrFilter.add(uniqueKey.getName());


        if (rb.doHighlights && null != rb.getHighlightQuery()) {
            String[] highlightFields = (String[]) rb.getDebugInfo().remove(MyHighlightComponent.HIGH_LIGHT_FIELDS);
            if (null != highlightFields && highlightFields.length > 0) {
                filedFilter.addAll(Arrays.asList(highlightFields));
            }
        }
        SolrIndexSearcher searcher = solrReq.getSearcher();

        DocIterator iterator = docList.iterator();

        Set<String> uniqueKeySet = new HashSet<>();

        Map<Integer, String> docMap = new HashMap<>();

        while (iterator.hasNext()) {
            int id = iterator.nextDoc();
            Document document = searcher.doc(id, solrFilter);
            String uid = document.get(uniqueKey.getName());
            uniqueKeySet.add(uid);
            docMap.put(id, uid);
        }


        logger.info("需要返回的字段 {} {} {}", filedFilter, isAllField, filedFilter);

        Map<String, Map<String, Object>> hBaseData = SolrHBaseUtils.getHBaseDataByRowKey(hBaseSolrData, schemaFieldMap, filedFilter, uniqueKeySet);

        logger.info("{} HBase 获取数据耗时 ......{} {} {}", Thread.currentThread(), coreName, System.currentTimeMillis() - start, hBaseData);

        HBaseDataThreadUtil.data(hBaseData, docMap, schemaFieldMap.keySet());
    }


    private static Set<String> solrFieldWant(Collection<SchemaField> schemaFields) {
        Set<String> filedFilter = new HashSet<>();
        List<String> fieldList = schemaFields.stream().filter(sf -> {
            String name = sf.getName();
            return !(name.startsWith("_") && name.endsWith("_")) && !sf.stored();
        }).map(SchemaField::getName)
                .collect(Collectors.toList());
        filedFilter.addAll(fieldList);
        return filedFilter;
    }

    @Override
    public void finishStage(ResponseBuilder rb) {
        if (!process) {
            return;
        }
        rb.getDebugInfo().remove(CORE_NAME);
    }

    @Override
    public String getDescription() {
        return "将数据从HBase 获取并设置到结果中";
    }
}
