package me.robin.datasource;

import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;

import java.lang.reflect.Method;

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
        if (null == dateSource) {
            dateSource = (DateSource) signature.getDeclaringType().getAnnotation(DateSource.class);
        }
        if (null == dateSource) {
            try {
                Method targetMethod = jp.getTarget().getClass().getMethod(signature.getName(), signature.getParameterTypes());
                dateSource = targetMethod.getAnnotation(DateSource.class);
            } catch (Throwable ignore) {

            }
        }
        if (null == dateSource || StringUtils.isBlank(dateSource.value())) {
            MultiDataSource.setDbKey(defaultDbKey);
        } else {
            MultiDataSource.setDbKey(dateSource.value());
        }
    }

}