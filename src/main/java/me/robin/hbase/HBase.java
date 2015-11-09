package me.robin.hbase;

import org.apache.hadoop.hbase.HTableDescriptor;

/**
 * Created by Lubin.Xuan on 2014/12/9.
 */
public class HBase {

    private static HTableDescriptor getHTableDescriptor1(String collection) {
        return new HTableDescriptor(collection);
    }

    private static HTableDescriptor getHTableDescriptor2(String collection) {
        return new HTableDescriptor(collection);
    }

    public static HTableDescriptor getHTableDescriptor(String collection) {
        if (HBaseSolrData.HBase_1) {
            return getHTableDescriptor1(collection);
        } else {
            return getHTableDescriptor2(collection);
        }
    }

}