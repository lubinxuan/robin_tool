package me.robin.mybatis;

import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

/**
 * Created by Lubin.Xuan on 2015/9/2.
 * ie.
 */
@Intercepts({
        @Signature(type = Executor.class, method = "update", args = {MappedStatement.class, Object.class}),
        @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class})
})
public class SqlExecuteLogInterceptor implements Interceptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(SqlExecuteLogInterceptor.class);

    private long warnLimit = 3000l;

    public Object intercept(Invocation invocation) throws Throwable {
        MappedStatement mappedStatement = (MappedStatement) invocation.getArgs()[0];
        Object parameter = null;
        if (invocation.getArgs().length > 1) {
            parameter = invocation.getArgs()[1];
        }
        BoundSql boundSql = mappedStatement.getBoundSql(parameter);
        Object returnValue;
        long start = System.currentTimeMillis();
        returnValue = invocation.proceed();
        long end = System.currentTimeMillis();
        long time = (end - start);
        if (time > 0) {
            String sql = boundSql.getSql().replaceAll("[\\s]+", " ");
            if (sql.length() > 100) {
                sql = sql.substring(0,100);
            }
            if (time > warnLimit) {
                LOGGER.warn("sql execute cost {} ms sql:[{}]", (end - start), sql);
            } else {
                LOGGER.debug("sql execute cost {} ms sql:[{}]", (end - start), sql);
            }
        }
        return returnValue;
    }

    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    public void setProperties(Properties properties) {
        String str = properties.getProperty("warnLimit", "3000");
        try {
            long tmp = Long.parseLong(str);
            if (tmp > 0) {
                warnLimit = tmp;
            }
        } catch (Exception ignore) {
        }
    }

}
