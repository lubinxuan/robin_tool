package me.robin.datasource;

import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;

/**
 * Created by Lubin.Xuan on 2015/9/1.
 * ie.
 */
public class DataSourceAdvice {

    private final String defaultDbKey;

    public DataSourceAdvice(String defaultDbKey) {
        this.defaultDbKey = defaultDbKey;
    }

    public void after(JoinPoint jp) {
        MultiDataSource.clearDbKey();
    }

    public void before(JoinPoint jp) {
        MethodSignature signature = ((MethodSignature) jp.getSignature());
        DateSource dateSource = signature.getMethod().getAnnotation(DateSource.class);
        if(null==dateSource){
            dateSource = (DateSource) signature.getDeclaringType().getAnnotation(DateSource.class);
        }
        if (null == dateSource || StringUtils.isBlank(dateSource.value())) {
            MultiDataSource.setDbKey(defaultDbKey);
        } else {
            MultiDataSource.setDbKey(dateSource.value());
        }
    }

}