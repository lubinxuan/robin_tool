package me.robin.solr.util;

import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.SolrInputField;
import org.apache.solr.core.SolrConfig;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.schema.SchemaField;
import org.apache.solr.update.AddUpdateCommand;
import org.apache.solr.update.DeleteUpdateCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Lubin.Xuan on 2015/3/3.
 * ie.
 */
public class SolrHBaseStore {

    private static final Logger logger = LoggerFactory.getLogger(SolrHBaseStore.class);

    private SolrQueryRequest req;

    private SchemaField uniqueKeyField;

    private HBaseSolrData hBaseSolrData;

    private List<SolrInputDocument> solrDocList = new ArrayList<>();
    private Set<String> uniqueIdDel = new HashSet<>();

    public SolrHBaseStore(SolrQueryRequest req, HBaseSolrData hBaseSolrData) {
        this.req = req;
        this.hBaseSolrData = hBaseSolrData;
        this.uniqueKeyField = req.getSchema().getUniqueKeyField();
    }

    public void add(AddUpdateCommand cmd) {
        solrDocList.add(cmd.getSolrInputDocument().deepCopy());
    }

    public void delete(DeleteUpdateCommand command) {
        uniqueIdDel.add(command.getId());
    }

    public void delete(String id) {
        uniqueIdDel.add(id);
    }

    public void delete(Set<String> id) {
        uniqueIdDel.addAll(id);
    }

    private static final Set<String> EXCLUDE_FIELD = new HashSet<>();

    static {
        EXCLUDE_FIELD.add("_version_");
    }

    //todo sendIndexData to HBase 暂时不处理删除操作
    public void sendHBase() throws Exception {

        SolrConfig solrConfig = req.getCore().getSolrConfig();
        RowKeyGenerator keyGenerator = SolrHBaseUtils.rowKeyGenerator(solrConfig);

        logger.debug("{}", this);

        Map<byte[], Map<String, Object>> inputDataMap = new HashMap<>();

        if (!solrDocList.isEmpty()) {

            StringBuilder stringBuilder = new StringBuilder("字段类型:[");

            boolean typ = false;

            for (SolrInputDocument doc : solrDocList) {
                EXCLUDE_FIELD.forEach(doc::removeField);
                String uniqueKeyValue = String.valueOf(doc.removeField(uniqueKeyField.getName()).getFirstValue());

                if (null != uniqueKeyValue && uniqueKeyValue.trim().length() > 0) {
                    Map<String, Object> dataItem = new HashMap<>();

                    for (SolrInputField f : doc.values()) {
                        if (null == f.getValue()) {
                            continue;
                        }

                        dataItem.put(f.getName(), f.getValue());
                        if (!typ) {
                            stringBuilder.append(f.getName()).append(":").append(f.getValue().getClass()).append(",");
                        }
                    }

                    if (!typ) {
                        typ = true;
                        stringBuilder.setLength(stringBuilder.length() - 1);
                        stringBuilder.append("]");

                        logger.debug("{}\n{}", stringBuilder, dataItem);
                    }

                    if (!dataItem.isEmpty()) {
                        inputDataMap.put(keyGenerator.rowKey(uniqueKeyValue.trim()), dataItem);
                    }
                }
            }
        }

        Set<byte[]> idSet = new HashSet<>();

        if (!uniqueIdDel.isEmpty()) {

            idSet.addAll(uniqueIdDel.stream().filter(id -> null != id && id.trim().length() > 0).map(keyGenerator::rowKey).collect(Collectors.toList()));

        }

        try {
            hBaseSolrData.batch(inputDataMap, idSet);
        } catch (Exception e) {
            logger.error("Hbase数据更新 异常!!!!", e);
            throw e;
        }
    }

    @Override
    public String toString() {
        return "HBase存储：{" +
                "core:" + req.getCore().getName() +
                "add:" + solrDocList.size() +
                ", delete:" + uniqueIdDel.size() +
                '}';
    }
}
