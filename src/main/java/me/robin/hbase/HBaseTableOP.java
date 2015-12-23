package me.robin.hbase;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.hadoop.hbase.util.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Lubin.Xuan on 2015/1/27.
 * ie.
 */
public class HBaseTableOP {

    private static final Logger logger = LoggerFactory.getLogger(HBaseTableOP.class);

    public static boolean createTable(Configuration configuration, String tableName, String[] familySet) throws Exception {
        HBaseAdmin hBaseAdmin = new HBaseAdmin(configuration);
        if (!hBaseAdmin.tableExists(tableName)) {
            HTableDescriptor tableDescriptor = HBase.getHTableDescriptor(tableName);
            int validFamilies = 0;
            for (String family : familySet) {
                if (null == family || family.trim().length() < 1) {
                    continue;
                }
                validFamilies++;
                tableDescriptor.addFamily(new HColumnDescriptor(family.trim()));
            }
            if (validFamilies < 1) {
                throw new IllegalArgumentException("HBase family required!!!!");
            }
            hBaseAdmin.createTable(tableDescriptor);
            return true;
        } else {
            logger.info("table [{}] already exists......", tableName);
        }
        return false;
    }

    public static boolean createTable(Configuration configuration, String tableName, String[] familySet, int regionCount) throws Exception {
        HBaseAdmin hBaseAdmin = new HBaseAdmin(configuration);
        try {
            if (!hBaseAdmin.tableExists(tableName)) {
                HTableDescriptor tableDescriptor = HBase.getHTableDescriptor(tableName);
                int validFamilies = 0;
                for (String family : familySet) {
                    if (null == family || family.trim().length() < 1) {
                        continue;
                    }
                    validFamilies++;
                    tableDescriptor.addFamily(new HColumnDescriptor(family.trim()));
                }
                if (validFamilies < 1) {
                    throw new IllegalArgumentException("HBase family required!!!!");
                }
                if (regionCount > 3) {
                    byte[][] spiltKeys = calcSplitKeys(regionCount);
                    hBaseAdmin.createTable(tableDescriptor, spiltKeys);
                } else {
                    hBaseAdmin.createTable(tableDescriptor);
                }
                return true;
            } else {
                logger.info("table [{}] already exists......", tableName);
            }
            return false;
        } finally {
            hBaseAdmin.close();
        }
    }

    public static byte[][] calcSplitKeys(int regionCount) {
        byte[][] splitKeys = new byte[regionCount - 1][];
        for (int i = 1; i < regionCount; i++) {
            splitKeys[i - 1] = Bytes.toBytes(i);
        }
        return splitKeys;
    }


    public static void deleteTable(Configuration configuration, String tableName) throws Exception {
        HBaseAdmin hAdmin = new HBaseAdmin(configuration);
        if (hAdmin.tableExists(tableName)) {
            hAdmin.disableTable(tableName);
            hAdmin.deleteTable(tableName);
        } else {
            logger.info("table [{}] not exists......", tableName);
        }
    }


    public static void enableTable(Configuration configuration, String tableName) throws Exception {
        HBaseAdmin hAdmin = new HBaseAdmin(configuration);
        if (hAdmin.tableExists(tableName)) {
            hAdmin.enableTable(tableName);
        } else {
            logger.info("table [{}] not exists......", tableName);
        }
    }
}
