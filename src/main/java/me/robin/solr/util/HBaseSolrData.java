package me.robin.solr.util;

import me.robin.hbase.BatchGet;
import me.robin.hbase.HBaseTableOP;
import me.robin.hbase.IObjectSerializer;
import me.robin.hbase.QualifierSet;
import me.robin.utils.CloseableUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HRegionLocation;
import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.lucene.util.BytesRef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private byte[] tableNameByte;

    private String collectionName;

    public static final String DATA_FAMILY = "d";

    private static final String TABLE_PREFIX = System.getProperty("table.prefix", "");
    private static final String TABLE_SUFFIX = System.getProperty("table.suffix", "_solr_store_data");

    private static final byte[] DATA_FAMILY_BYTE = Bytes.toBytes(DATA_FAMILY);

    private HTablePool hTablePool;

    public HBaseSolrData(Configuration configuration, String collectionName) throws Exception {
        assert StringUtils.isNotBlank(collectionName) : "必须指定Solr Collection 名称";
        assert null != configuration : "必须指定HBase基础配置信息";
        this.configuration = configuration;
        this.collectionName = collectionName.trim();
        this.tableName = getTableName(collectionName.trim());
        this.tableNameByte = Bytes.toBytes(this.tableName);
        hTablePool = new HTablePool(configuration, 512);
    }

    public HBaseSolrData(Configuration configuration, String collectionName, int regionCount) throws Exception {
        this(configuration, collectionName);
        createTable(regionCount);
    }

    private void createTable(int regionCount) {
        logger.info("start create table for collection [{}]......", this.collectionName);
        try {
            if (HBaseTableOP.createTable(configuration, tableName, new String[]{DATA_FAMILY}, regionCount)) {
                logger.info("success create table for collection [{}]......", this.collectionName);
            }
        } catch (Exception e) {
            logger.error("failed create table for collection [" + this.collectionName + "]......", e);
        }
    }

    public static String getTableName(String coreName) {
        return TABLE_PREFIX + coreName + TABLE_SUFFIX;
    }

    /**
     * 单条件查询,根据rowkey查询唯一一条记录
     */
    public <T extends Map<String, Object>> Map<String, T> getByRowKeyColl(Collection<byte[]> rowKeyColl, HBaseMapper<T> mapper) {
        return getByRowKeyColl(rowKeyColl, mapper, null);
    }

    /**
     * 单条件查询,根据rowkey查询唯一一条记录
     */
    public <T extends Map<String, Object>> Map<String, T> getByRowKeyColl(Collection<byte[]> rowKeyColl, HBaseMapper<T> mapper, QualifierSet qualifierSet) {
        if (null == rowKeyColl || rowKeyColl.isEmpty()) {
            return Collections.emptyMap();
        }
        qualifierSet = null == qualifierSet ? new QualifierSet() : qualifierSet;
        try {
            HConnection hConnection = HConnectionManager.getConnection(this.configuration);
            HashMap<HRegionLocation, List<Get>> hRegionLocationListHashMap = new HashMap<>();
            int row_count = 0;
            for (byte[] rowKey : rowKeyColl) {
                if (null != rowKey) {
                    Get get = qualifierSet.fillQualifier(new Get(rowKey));
                    HRegionLocation row = hConnection.locateRegion(tableNameByte, rowKey);
                    List<Get> e = hRegionLocationListHashMap.get(row);
                    if (e == null) {
                        e = new ArrayList<>();
                        hRegionLocationListHashMap.put(row, e);
                    }
                    e.add(get);
                    row_count++;
                }
            }
            if (row_count < 1) {
                return Collections.emptyMap();
            }
            long start = System.currentTimeMillis();
            Map<String, T> tList = new HashMap<String, T>();
            List<Result> resultList = BatchGet.get2(hRegionLocationListHashMap, tableName, hTablePool);
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

        Map<String, BytesRef> dataMap = new HashMap<String, BytesRef>();
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
            return mapper.mapper(r.getRow(), dataMap);
        } else {
            return null;
        }
    }

    public List<Put> preparePut(Map<byte[], ? extends Map<String, Object>> dataMap) {
        if (null == dataMap || dataMap.isEmpty()) {
            return Collections.emptyList();
        }
        List<Put> putList = new LinkedList<Put>();
        for (Map.Entry<byte[], ? extends Map<String, Object>> entry : dataMap.entrySet()) {
            if (null == entry.getKey() || null == entry.getValue() || entry.getValue().isEmpty()) {
                continue;
            }
            Put put = buildPut(entry.getKey(), entry.getValue());
            putList.add(put);
        }
        return putList;
    }

    public List<Delete> prepareDelete(Collection<byte[]> rowKeyColl) {
        if (null == rowKeyColl || rowKeyColl.isEmpty()) {
            return Collections.emptyList();
        }
        List<Delete> deleteList = new ArrayList<Delete>();

        for (byte[] rowKey : rowKeyColl) {

            Delete delete = new Delete(rowKey);

            deleteList.add(delete);

        }
        return deleteList;
    }


    public void insertData(Map<byte[], ? extends Map<String, Object>> dataMap) throws Exception {
        if (null == dataMap || dataMap.isEmpty()) {
            return;
        }
        List<Put> putList = preparePut(dataMap);

        if (!putList.isEmpty()) {
            long st = System.currentTimeMillis();
            HTableInterface hTableInterface = getTableInterface();
            logger.debug("开始将数据存入HBase ......data size[{}] table 接口获取耗时[{}] ms", putList.size(), System.currentTimeMillis() - st);
            long start = System.currentTimeMillis();
            try {
                hTableInterface.put(putList);
                hTableInterface.flushCommits();
            } finally {
                CloseableUtils.closeQuietly(hTableInterface);
            }

            logger.info("data[{}] HBase 数据存储耗时[{}]ms", putList.size(), System.currentTimeMillis() - start);
        }
    }


    public void deleteData(Collection<byte[]> rowKeyColl) throws Exception {
        if (null == rowKeyColl || rowKeyColl.isEmpty()) {
            return;
        }

        long start = System.currentTimeMillis();

        List<Delete> deleteList = prepareDelete(rowKeyColl);

        HTableInterface hTableInterface = getTableInterface();
        try {
            hTableInterface.delete(deleteList);
            hTableInterface.flushCommits();
        } finally {
            CloseableUtils.closeQuietly(hTableInterface);
        }


        logger.info("删除HBase 数据: [{}] {} 耗时[{}]ms", deleteList.size(), deleteList, System.currentTimeMillis() - start);
    }

    public void batch(Map<byte[], ? extends Map<String, Object>> dataMap, Collection<byte[]> rowKeyColl) throws Exception {
        long start = System.currentTimeMillis();
        List<Put> putList = preparePut(dataMap);
        List<Delete> deleteList = prepareDelete(rowKeyColl);
        int add = putList.size(), del = deleteList.size();
        logger.info("开始更新HBase ...... add:[{}] del:[{}]", add, del);
        batch(putList, deleteList);
        logger.info("更新HBase 完成 cost:[{}] ms...... add:[{}] del:[{}]", System.currentTimeMillis() - start, add, del);
    }

    private void batch(List<Put> addList, List<Delete> delList) throws Exception {
        HTableInterface hTableInterface = getTableInterface();
        try {
            List<Row> rowList = new ArrayList<Row>();
            if (!addList.isEmpty()) {
                rowList.addAll(addList);
            }
            if (!delList.isEmpty()) {
                rowList.addAll(delList);
            }
            hTableInterface.batch(rowList);
            hTableInterface.flushCommits();
        } finally {
            CloseableUtils.closeQuietly(hTableInterface);
        }
    }

    private Put buildPut(byte[] rowKey, Map<String, Object> data) {
        Put put = new Put(rowKey);// 一个PUT代表一行数据，再NEW一个PUT表示第二行数据,每行一个唯一的ROWKEY，此处rowkey为put构造方法中传入的值
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

    private HTableInterface getTableInterface() {
        HTableInterface tableInterface = this.hTablePool.getTable(tableName);
        tableInterface.setAutoFlush(false);
        return tableInterface;
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