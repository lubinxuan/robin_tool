package me.robin.hbase;

import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.lucene.util.BytesRef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

/**
 * Created by Lubin.Xuan on 2014/12/9.
 */
public class HBaseSolrData {

    private static final Logger logger = LoggerFactory.getLogger(HBaseSolrData.class);
    public static final boolean HBase_1 = "1".equals(System.getProperty("v.hbase"));

    private Configuration configuration;

    private String tableName;

    private String collectionName;

    public static final String DATA_FAMILY = "indexData";

    private static final byte[] DATA_FAMILY_BYTE = Bytes.toBytes(DATA_FAMILY);

    private HTablePool hTablePool;

    public HBaseSolrData(Configuration configuration, String collectionName) throws Exception {
        assert StringUtils.isNotBlank(collectionName) : "必须指定Solr Collection 名称";
        assert null != configuration : "必须指定HBase基础配置信息";
        this.configuration = configuration;
        this.collectionName = collectionName.trim();
        this.tableName = getTableName(collectionName.trim());
        createTable();
        hTablePool = new HTablePool(configuration, 512);
    }

    private void createTable() {
        logger.info("start create table for collection [{}]......", this.collectionName);
        try {
            if (HBaseTableOP.createTable(configuration, tableName, new String[]{DATA_FAMILY})) {
                logger.info("success create table for collection [{}]......", this.collectionName);
            }
        } catch (Exception e) {
            logger.error("failed create table for collection [" + this.collectionName + "]......", e);
        }
    }

    public static String getTableName(String coreName) {
        return coreName + "_solr_store_data";
    }

    /**
     * 单条件查询,根据rowkey查询唯一一条记录
     */
    public <T extends Map<String, Object>> Map<String, T> getByRowKeyColl(Collection<String> rowKeyColl, HBaseMapper<T> mapper, QualifierSet qualifierSet) {
        if (null == rowKeyColl || rowKeyColl.isEmpty()) {
            return Collections.emptyMap();
        }
        qualifierSet = null == qualifierSet ? new QualifierSet() : qualifierSet;
        try {
            List<Get> getList = new ArrayList<Get>();
            for (String rowKey : rowKeyColl) {
                if (StringUtils.isNotBlank(rowKey)) {
                    getList.add(qualifierSet.fillQualifier(new Get(Bytes.toBytes(rowKey))));
                }
            }
            if (getList.isEmpty()) {
                return Collections.emptyMap();
            }
            long start = System.currentTimeMillis();
            Map<String, T> tList = new HashMap<>();
            //Result[] resultArr = table.get(getList);
            List<Result> resultList = BatchGet.get(getList, tableName, hTablePool);
            boolean isAllQualifier = qualifierSet.getQualifierSet().isEmpty();
            for (Result r : resultList) {
                Entry<T> tmp = parseResult(mapper, qualifierSet, isAllQualifier, r);
                if (null != tmp && null != tmp.getValue()) {
                    tList.put(tmp.getKey(), tmp.getValue());
                }
            }
            logger.info("cost time {}ms 获得到valueCount:{}", System.currentTimeMillis() - start, tList.size());
            return tList;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("HBase 查询数据异常 m:[{}] e:[{}] ", e.getMessage(), e.toString());
            return Collections.emptyMap();
        }
    }


    private <T> Entry<T> parseResult(HBaseMapper<T> mapper, QualifierSet qualifierSet, boolean isAllQualifier, Result r) {

        if (null == r || null == r.list()) {
            return null;
        }

        Map<String, BytesRef> dataMap = new HashMap<>();
        if (isAllQualifier) {
            for (KeyValue kv : r.list()) {
                dataMap.put(Bytes.toString(kv.getQualifier()), new BytesRef(kv.getValue()));
            }
        } else {
            for (int i = 0; i < qualifierSet.getQualifierByte().size(); i++) {
                if (r.containsColumn(qualifierSet.getFamilyByte(), qualifierSet.getQualifierByte().get(i))) {
                    byte[] valueByte = r.getValue(qualifierSet.getFamilyByte(), qualifierSet.getQualifierByte().get(i));
                    dataMap.put(qualifierSet.getQualifierSet().get(i), new BytesRef(valueByte));
                }
            }
        }
        if (!dataMap.isEmpty()) {
            return mapper.mapper(Bytes.toString(r.getRow()), dataMap);
        } else {
            return null;
        }
    }

    public List<Put> preparePut(Map<String, ? extends Map<String, Object>> dataMap) {
        if (null == dataMap || dataMap.isEmpty()) {
            return Collections.emptyList();
        }
        List<Put> putList = new LinkedList<>();
        for (Map.Entry<String, ? extends Map<String, Object>> entry : dataMap.entrySet()) {
            if (StringUtils.isBlank(entry.getKey()) || null == entry.getValue() || entry.getValue().isEmpty()) {
                continue;
            }
            Put put = buildPut(entry.getKey(), entry.getValue());
            putList.add(put);
        }
        return putList;
    }

    public List<Delete> prepareDelete(Collection<String> rowKeyColl) {
        if (null == rowKeyColl || rowKeyColl.isEmpty()) {
            return Collections.emptyList();
        }
        List<Delete> deleteList = new ArrayList<>();

        for (String rowKey : rowKeyColl) {

            Delete delete = new Delete(Bytes.toBytes(rowKey));

            deleteList.add(delete);

        }
        return deleteList;
    }


    public void insertData(Map<String, ? extends Map<String, Object>> dataMap) throws Exception {
        if (null == dataMap || dataMap.isEmpty()) {
            return;
        }
        List<Put> putList = preparePut(dataMap);
        logger.info("开始将数据存入HBase ......data size[{}]", putList.size());
        long start = System.currentTimeMillis();
        if (!putList.isEmpty()) {
            HTableInterface hTableInterface = hTablePool.getTable(tableName);
            try {
                hTableInterface.put(putList);
            } finally {
                hTableInterface.close();
            }

            logger.info("data[{}] HBase 数据存储耗时[{}]ms", putList.size(), System.currentTimeMillis() - start);
        }
        logger.debug("HBase 数据存储 完成 ......");
    }


    public void deleteData(Collection<String> rowKeyColl) throws Exception {
        if (null == rowKeyColl || rowKeyColl.isEmpty()) {
            return;
        }

        long start = System.currentTimeMillis();

        List<Delete> deleteList = prepareDelete(rowKeyColl);

        HTableInterface hTableInterface = hTablePool.getTable(tableName);
        try {
            hTableInterface.delete(deleteList);
        } finally {
            hTableInterface.close();
        }


        logger.info("删除HBase 数据: [{}] {} 耗时[{}]ms", deleteList.size(), deleteList, System.currentTimeMillis() - start);
    }

    public void batch(Map<String, ? extends Map<String, Object>> dataMap,Collection<String> rowKeyColl) throws Exception {
        long start = System.currentTimeMillis();
        List<Put> putList = preparePut(dataMap);
        List<Delete> deleteList = prepareDelete(rowKeyColl);
        int add = putList.size(), del = deleteList.size();
        logger.info("开始更新HBase ...... add:[{}] del:[{}]", add, del);
        batch(putList,deleteList);
        logger.info("更新HBase 完成 cost:[{}] ms...... add:[{}] del:[{}]", System.currentTimeMillis() - start, add, del);
    }

    private void batch(List<Put> addList, List<Delete> delList) throws Exception {
        HTableInterface hTableInterface = hTablePool.getTable(tableName);
        try {
            List<Row> rowList = new ArrayList<>();
            if(!addList.isEmpty()){
                rowList.addAll(addList);
            }
            if(!delList.isEmpty()){
                rowList.addAll(delList);
            }
            hTableInterface.batch(rowList);
        } finally {
            hTableInterface.close();
        }
    }

    private Put buildPut(String rowKey, Map<String, Object> data) {
        Put put = new Put(Bytes.toBytes(rowKey));// 一个PUT代表一行数据，再NEW一个PUT表示第二行数据,每行一个唯一的ROWKEY，此处rowkey为put构造方法中传入的值
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            Object value = entry.getValue();
            byte[] byteValue = null;
            if (null == value) {
                byteValue = null;
            } else if (value instanceof String) {
                byteValue = Bytes.toBytes((String) value);
            } else if (value instanceof Number) {
                if (value instanceof Integer) {
                    byteValue = Bytes.toBytes((Integer) value);
                } else if (value instanceof Float) {
                    byteValue = Bytes.toBytes((Float) value);
                } else if (value instanceof Double) {
                    byteValue = Bytes.toBytes((Double) value);
                } else if (value instanceof Long) {
                    byteValue = Bytes.toBytes((Long) value);
                } else if (value instanceof Short) {
                    byteValue = Bytes.toBytes((Short) value);
                } else if (value instanceof Byte) {
                    byteValue = Bytes.toBytes((Byte) value);
                } else if (value instanceof BigDecimal) {
                    byteValue = Bytes.toBytes((BigDecimal) value);
                } else {
                    byteValue = Bytes.toBytes(String.valueOf(value));
                }
            } else if (value instanceof Boolean) {
                byteValue = Bytes.toBytes((Boolean) value);
            } else if (value.getClass().isArray() || Iterable.class.isAssignableFrom(value.getClass())) {
                byteValue = IObjectSerializer.serialize(value);
            }
            put.add(DATA_FAMILY_BYTE, Bytes.toBytes(entry.getKey()), byteValue);
        }
        return put;
    }

    public static class Entry<T> {

        String key;
        T value;

        public Entry(String key, T value) {
            this.key = key;
            this.value = value;
        }

        public String getKey() {
            return key;
        }

        public T getValue() {
            return value;
        }
    }
}