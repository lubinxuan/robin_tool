package me.robin.datasource;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

import java.util.Stack;

/**
 * Created by Lubin.Xuan on 2015/9/1.
 * ie.
 */
public class MultiDataSource extends AbstractRoutingDataSource {

    private static final ThreadLocal<String> DATASOURCE_KEY_2 = new ThreadLocal<String>();
    private static final ThreadLocal<Stack<String>> DATASOURCE_KEY = new ThreadLocal<Stack<String>>() {
        @Override
        protected Stack<String> initialValue() {
            return new Stack<String>();
        }
    };

    public static void clearDbKey() {
        Stack<String> stack = DATASOURCE_KEY.get();
        stack.pop();
    }

    public static void setDbKey(String dbKey) {
        DATASOURCE_KEY.get().push(dbKey);
    }

    @Override
    protected Object determineCurrentLookupKey() {
        return DATASOURCE_KEY.get().peek();
    }
}
