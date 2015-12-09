package me.robin.solr.util;

/**
 * Created by Administrator on 2015/12/9.
 */
public interface RowKeyGenerator {
    public byte[] rowKey(String id);

    public String rowKey(byte[] byteData);
}
