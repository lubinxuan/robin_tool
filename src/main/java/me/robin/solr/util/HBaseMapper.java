package me.robin.solr.util;

import me.robin.solr.util.HBaseSolrData;
import org.apache.lucene.util.BytesRef;

import java.util.Map;

/**
 * Created by Lubin.Xuan on 2014/12/10.
 */
public interface HBaseMapper<T> {
    public HBaseSolrData.Entry<T> mapper(byte[] rowKey, Map<String, BytesRef> dataMap);
}
