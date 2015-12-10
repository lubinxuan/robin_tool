package me.robin.hbase;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;

public class Configure {
    private static Configuration configuration;

    static {
        configuration = HBaseConfiguration.create();
        configuration.set("hbase.zookeeper.property.clientPort", System.getProperty("hbase.zk.port", "2181"));
        configuration.set("hbase.zookeeper.quorum", System.getProperty("hbase.zk.ip", "dc-hadoop120,dc-hadoop121,dc-hadoop122"));
        configuration.set("hbase.master.port", System.getProperty("hbase.master.port", "60000"));
        configuration.set("hbase.client.write.buffer", "" + 10 * 1024 * 1024);//10M 写入buffer
        /*configuration.set("hbase.master", System.getProperty("hbase.master", "dc-hadoop117"));*/
    }

    public static Configuration getConfiguration() {
        return configuration;
    }
}
