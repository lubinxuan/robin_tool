package me.robin.datasource;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

/**
 * Created by Lubin.Xuan on 2015/9/1.
 * ie.
 */
public class MultiDataSource extends AbstractRoutingDataSource {

    private static final ThreadLocal<String> DATASOURCE_KEY = new ThreadLocal<String>();

    public static void clearDbKey() {
        DATASOURCE_KEY.remove();
    }

    public static void setDbKey(String dbKey) {
        DATASOURCE_KEY.set(dbKey);
    }

    @Override
    protected Object determineCurrentLookupKey() {
        return DATASOURCE_KEY.get();
    }
}
