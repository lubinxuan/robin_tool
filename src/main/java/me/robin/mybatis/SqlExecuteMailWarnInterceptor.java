package me.robin.mybatis;

import me.robin.Const;
import me.robin.mail.SimpleMailSender;
import me.robin.utils.IpUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Lubin.Xuan on 2015/9/2.
 * ie.
 */
@Intercepts({
        @Signature(type = Executor.class, method = "update", args = {MappedStatement.class, Object.class}),
        @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class})
})
public class SqlExecuteMailWarnInterceptor implements Interceptor {

    private static final Logger logger = LoggerFactory.getLogger(SqlExecuteMailWarnInterceptor.class);

    private static final String HOST = IpUtils.getLocalIp();

    private SimpleMailSender simpleMailSender = null;
    private List<String> recipients = new ArrayList<String>();

    public SqlExecuteMailWarnInterceptor() {
        Properties prop = new Properties();
        try {
            InputStream is = this.getClass().getClassLoader().getResourceAsStream("mybatis.properties");
            BufferedReader bf = new BufferedReader(new InputStreamReader(is, Const.UTF_8));
            prop.load(bf);
            bf.close();
        } catch (Throwable ignore) {
            logger.warn("sql 出错预警配置没有找到!!!! mybatis.properties");
        }
        //properties;
        String smtp = prop.getProperty("warn.mail.server");
        String user = prop.getProperty("warn.mail.sender");
        String senderViewName = prop.getProperty("warn.mail.senderViewName");
        String pwd = prop.getProperty("warn.mail.password");
        if (StringUtils.isBlank(user) && StringUtils.isBlank(pwd)) {
            return;
        }
        String recipients = prop.getProperty("warn.mail.recipients");
        String[] arr = recipients.split(",");
        for (String i : arr) {
            if (StringUtils.isNotBlank(i)) {
                this.recipients.add(i);
            }
        }
        if (StringUtils.isBlank(smtp)) {
            this.simpleMailSender = new SimpleMailSender(user, pwd, senderViewName);
        } else {
            this.simpleMailSender = new SimpleMailSender(smtp, user, pwd, senderViewName);
        }
    }

    public Object intercept(Invocation invocation) throws Throwable {
        MappedStatement mappedStatement = (MappedStatement) invocation.getArgs()[0];
        Object parameter = null;
        if (invocation.getArgs().length > 1) {
            parameter = invocation.getArgs()[1];
        }
        BoundSql boundSql = mappedStatement.getBoundSql(parameter);
        String sql = boundSql.getSql().replaceAll("[\\s]+", " ");
        Object returnValue;
        try {
            returnValue = invocation.proceed();
            try {
                WARNING_LOG_MAP.remove(Base64.getEncoder().encodeToString(sql.getBytes(Const.UTF_8)));
            } catch (Throwable ignore) {
            }
        } catch (Throwable r) {
            sendMail(sql, mappedStatement.getId(), mappedStatement.getResource(), r);
            throw r;
        }
        return returnValue;
    }

    private static final ExecutorService SERVICE = Executors.newFixedThreadPool(10);

    private static final Map<String, WarningLog> WARNING_LOG_MAP = new ConcurrentHashMap<String, WarningLog>();

    private void sendMail(final String sql, final String method, final String mapperFile, final Throwable r) {
        if (null == simpleMailSender || this.recipients.isEmpty()) {
            return;
        }
        SERVICE.execute(new Runnable() {
            public void run() {
                String key = Base64.getEncoder().encodeToString(sql.getBytes(Const.UTF_8));
                WarningLog warningLog = WARNING_LOG_MAP.get(key);
                if (null == warningLog) {
                    synchronized (WARNING_LOG_MAP) {
                        if (!WARNING_LOG_MAP.containsKey(key)) {
                            warningLog = new WarningLog();
                            WARNING_LOG_MAP.put(key, warningLog);
                        } else {
                            warningLog = WARNING_LOG_MAP.get(key);
                        }
                    }
                }

                if (!warningLog.send()) {
                    return;
                }

                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                try {
                    final StringBuilder content = new StringBuilder("SQL异常报警!!!<br/>");
                    content.append("服务器:").append(HOST).append("<br/>");
                    content.append("配置文件:").append(mapperFile).append("<br/>");
                    content.append("方法:").append(method).append("<br/>");
                    content.append("异常发生次数:").append(warningLog.errorCount()).append("<br/>");
                    content.append("SQL:").append(sql).append("<br/>");
                    r.printStackTrace(new PrintStream(outputStream,false,Const._UTF_8));
                    content.append(outputStream.toString(Const._UTF_8).replaceAll("\n", "<br/>").replaceAll("\t", "&nbsp;&nbsp;&nbsp;").replaceAll(" ", "&nbsp;"));
                    simpleMailSender.send(recipients, "SQL异常报警-" + method, content);
                } catch (Throwable ignore) {
                } finally {
                    try {
                        outputStream.close();
                    } catch (IOException ignore) {
                    }
                }
            }
        });
    }

    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    public void setProperties(Properties properties) {

    }

}
