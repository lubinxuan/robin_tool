package me.robin.mybatis;

import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Created by Lubin.Xuan on 2015/2/14.
 * ie.
 */
public class MybatisExecutor {

    private static final Logger logger = LoggerFactory.getLogger(MybatisExecutor.class);

    private SqlSessionFactory sqlSessionFactory;

    private int batchSize = 10000;

    private void init() {
        this.batchSize = batchSize < 1 || batchSize > 10000 ? 10000 : batchSize;
    }

    public <M extends BatchMapper<T>, T> int executeBatch(Class<M> mapperClass, List<T> tList, Type type) {

        if (null == tList || tList.isEmpty() || null == type) {
            return 0;
        }

        int updateSize = 0;

        try {

            M mapper = startSession(mapperClass, ExecutorType.BATCH, false);

            for (int i = 0; i < tList.size(); i++) {
                if (Type.INSERT.equals(type)) {
                    mapper.insert(tList.get(i));
                } else {
                    mapper.update(tList.get(i));
                }

                if ((i != 0 && i % batchSize == 0) || i + 1 == tList.size()) {
                    commitSession();
                    updateSize = i + 1;
                }
            }
        } catch (Exception e) {

            logger.error("批量[" + type + "]数据时发生异常 提交{}/{}", e, updateSize, tList.size());

            return updateSize;
        } finally {
            closeSession();
        }
        return updateSize;
    }


    public static enum Type {
        INSERT, UPDATE
    }


    private static final ThreadLocal<SqlSession> SESSION_THREAD_LOCAL = new ThreadLocal<SqlSession>();

    public <M> M startSession(Class<M> mapperClass) {

        if (null == mapperClass) {
            return null;
        }

        SESSION_THREAD_LOCAL.set(SqlSessionUtils.getSqlSession(sqlSessionFactory));

        return SESSION_THREAD_LOCAL.get().getMapper(mapperClass);

    }

    public <M> M startSession(Class<M> mapperClass, ExecutorType executorType) {

        if (null == mapperClass) {
            return null;
        }

        SESSION_THREAD_LOCAL.set(SqlSessionUtils.getSqlSession(sqlSessionFactory, executorType, null));

        return SESSION_THREAD_LOCAL.get().getMapper(mapperClass);

    }

    public <M> M startSession(Class<M> mapperClass, ExecutorType executorType, boolean autoCommit) {

        if (null == mapperClass) {
            return null;
        }

        SESSION_THREAD_LOCAL.set(sqlSessionFactory.openSession(executorType, autoCommit));

        return SESSION_THREAD_LOCAL.get().getMapper(mapperClass);

    }

    public void commitSession() {
        if (null != SESSION_THREAD_LOCAL.get()) {
            SESSION_THREAD_LOCAL.get().commit();
            SESSION_THREAD_LOCAL.get().clearCache();
        }
    }

    public void closeSession() {
        if (null != SESSION_THREAD_LOCAL.get()) {
            SqlSessionUtils.closeSqlSession(SESSION_THREAD_LOCAL.get(), sqlSessionFactory);
            SESSION_THREAD_LOCAL.remove();
        }
    }

    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }

    public void setSqlSessionFactory(SqlSessionFactory sqlSessionFactory) {
        this.sqlSessionFactory = sqlSessionFactory;
    }
}
