package me.robin.hbase;

import org.apache.lucene.util.BytesRef;

import java.util.Map;

/**
 * Created by Lubin.Xuan on 2014/12/10.
 */
public interface HBaseMapper<T> {
    public HBaseSolrData.Entry<T> mapper(String rowKey, Map<String, BytesRef> dataMap);
}
