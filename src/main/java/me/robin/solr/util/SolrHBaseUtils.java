package me.robin.solr.util;

import me.robin.hbase.*;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.lucene.util.BytesRef;
import org.apache.solr.core.SolrConfig;
import org.apache.solr.schema.SchemaField;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Created by Lubin.Xuan on 2015/1/28.
 * ie.
 */
public class SolrHBaseUtils {

    private static final Logger logger = LoggerFactory.getLogger(SolrHBaseUtils.class);

    private static final Set<String> H_BASE_STORE_CORE = new HashSet<>();

    private static final boolean ALL_CORE_HABSE;

    static {
        String core_in_hbase = System.getProperty("hbase.store.core", "admonitor,weibo");
        if (null != core_in_hbase && core_in_hbase.trim().length() > 0) {
            for (String core : core_in_hbase.split(",")) {
                if (null != core && core.trim().length() > 0) {
                    H_BASE_STORE_CORE.add(core.trim());
                }
            }
        }
        String all = System.getProperty("all.core.hbase", "false");
        ALL_CORE_HABSE = Boolean.valueOf(all);
    }

    public static String getCoreAliens(String origin) {
        if (origin.contains(CORE_NAME_SPILT)) {
            return origin.split(CORE_NAME_SPILT)[0];
        } else {
            return origin;
        }
    }

    public static boolean isHBaseStoreCore(String coreName) {
        //return ALL_CORE_HABSE || H_BASE_STORE_CORE.contains(getCoreAliens(coreName));
        return true;
    }

    private static final String CORE_NAME_SPILT = "_shard";

    public static Map<String, Map<String, Object>> getHBaseDataByRowKey(HBaseSolrData hBaseSolrData, Map<String, SchemaField> schemaFieldMap, Set<String> filedFilter, Set<String> docIdSet, RowKeyGenerator keyGenerater) {
        logger.debug("从HBase 查询 返回 字段 {}", filedFilter);
        QualifierSet qualifierSet = new QualifierSet(filedFilter);
        HBaseMapper<Map<String, Object>> baseMapper = (rowKey, dataMap) -> {
            Map<String, Object> resultMap = new HashMap<>();
            for (Map.Entry<String, BytesRef> entry : dataMap.entrySet()) {
                String fieldName = entry.getKey();
                SchemaField field = schemaFieldMap.get(fieldName);
                BytesRef bytesRef = entry.getValue();
                Object realValue = null;
                String typeName = field.getType().getTypeName();
                try {
                    if (field.multiValued()) {
                        realValue = IObjectSerializer.deserialize(bytesRef.bytes);
                    } else if (typeName.contains("int")) {
                        realValue = Bytes.toInt(bytesRef.bytes);
                    } else if (typeName.contains("double")) {
                        realValue = Bytes.toDouble(bytesRef.bytes);
                    } else if (typeName.contains("float")) {
                        realValue = Bytes.toFloat(bytesRef.bytes);
                    } else if (typeName.contains("long")) {
                        realValue = Bytes.toLong(bytesRef.bytes);
                    } else if (typeName.contains("date")) {
                        realValue = new DateTime(Bytes.toString(bytesRef.bytes)).toDate();
                    }
                    if (null == realValue) {
                        realValue = Bytes.toString(bytesRef.bytes);
                    }
                    logger.debug("字段 {} 值 {}", field, realValue);
                } catch (Exception e) {
                    logger.error("字段" + fieldName + " 值转换出错 " + e.toString() + "!!!", e);
                }
                resultMap.put(fieldName, realValue);
            }
            return new HBaseSolrData.Entry<>(keyGenerater.rowKey(rowKey), resultMap);
        };

        Collection<byte[]> rowKyeByte = docIdSet.stream().map(keyGenerater::rowKey).collect(Collectors.toCollection(ArrayList::new));

        return hBaseSolrData.getByRowKeyColl(rowKyeByte, baseMapper, qualifierSet);
    }


    private static final Map<Integer, RowKeyGenerator> KEY_GENERATOR_CACHE = new ConcurrentHashMap<>();

    public static RowKeyGenerator rowKeyGenerator(SolrConfig config) {
        final int regionCount = regionCountDefine(config);
        return rowKeyGenerator(regionCount);
    }

    public static RowKeyGenerator rowKeyGenerator(final int regionCount) {
        return KEY_GENERATOR_CACHE.compute(regionCount, (integer, rowKeyGenerator) -> {
            if (null == rowKeyGenerator) {
                return new RowKeyGenerator() {
                    @Override
                    public byte[] rowKey(String id) {
                        byte[] key_b = Bytes.toBytes(id);
                        /*if (key_b.length < 4) {
                            byte[] _key_b = Bytes.padHead(key_b, 4 - key_b.length);
                            int _id = Bytes.toInt(_key_b) % regionCount;
                            return Bytes.add(Bytes.toBytes(_id), key_b);
                        } else {*/
                        int _id = id.hashCode() % regionCount;
                        return Bytes.add(Bytes.toBytes(_id), key_b);
                        //}
                    }

                    @Override
                    public String rowKey(byte[] byteData) {
                        byte[] key = Bytes.tail(byteData, byteData.length - 4);
                        return Bytes.toString(key);
                    }
                };
            }
            return rowKeyGenerator;
        });
    }

    private static int regionCountDefine(SolrConfig config) {
        if (null == config) {
            return 50;
        } else {
            return config.getInt("config/hbase/regionCount", 50);
        }
    }

    public synchronized static HBaseSolrData get(String core, SolrConfig config) {
        HBaseSolrData hBaseSolrData = CollectionStore.get(core);
        if (null == hBaseSolrData && !CollectionStore.failCore(core)) {
            int regionCount = regionCountDefine(config);
            if (regionCount > 3) {
                CollectionStore.initialize(core, regionCount);
            } else {
                CollectionStore.initialize(core);
            }
            return CollectionStore.get(core);
        } else {
            return hBaseSolrData;
        }
    }

}