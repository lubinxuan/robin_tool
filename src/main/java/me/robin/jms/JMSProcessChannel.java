package me.robin.jms;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by xuanlubin on 2016/6/8.
 */
public class JMSProcessChannel<T> implements AutoCloseable {

    private static final Logger logger = LoggerFactory.getLogger(JMSProcessChannel.class);

    private static final String DEFAULT_TMP_STORE_PATH = "./jms_tmp.dat";

    private File tmpStore;

    private ExecutorService service;

    private ChannelHandler<T> consumer;

    private BlockingQueue<T> dataQueue = new LinkedBlockingQueue<>();

    private boolean shutdown = false;

    private Thread thread = null;

    protected JMSProcessChannel(ChannelHandler<T> consumer, String fileStore) {
        this.consumer = consumer;
        this.tmpStore = new File(fileStore);
    }

    protected JMSProcessChannel(ChannelHandler<T> consumer) {
        this(consumer, DEFAULT_TMP_STORE_PATH);
    }

    public void setService(ExecutorService service) {
        this.service = service;
    }

    public void offer(T data) {
        if (null == data) {
            return;
        }
        dataQueue.offer(data);
    }

    public synchronized void start() {
        if (null != thread) {
            return;
        }
        this.load();
        thread = new Thread(() -> {
            while (!shutdown) {
                if (!consumer.available()) {
                    try {
                        TimeUnit.MILLISECONDS.sleep(500);
                    } catch (InterruptedException e) {
                        continue;
                    }
                }

                T t;
                try {
                    t = dataQueue.take();
                } catch (InterruptedException e) {
                    continue;
                }

                if (null != service) {
                    service.execute(() -> exec(t));
                } else {
                    exec(t);
                }
            }
            save();
            if (null != service) {
                service.shutdown();
                try {
                    if (!service.awaitTermination(5, TimeUnit.SECONDS)) {
                        logger.warn("等待处理线程退出~~~~");
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }, "JMS_Channel_" + tmpStore.getAbsolutePath());

        thread.start();
    }

    private void exec(T data) {
        if (!consumer.handle(data)) {
            logger.info("任务处理异常，重新入列");
            dataQueue.offer(data);
        }
    }

    private void load() {

        if (!tmpStore.exists() || tmpStore.isDirectory()) {
            return;
        }

        logger.info("开始加载临时数据");
        try {
            Type type = ((ParameterizedType) this.getClass().getGenericSuperclass()).getActualTypeArguments()[0];
            List<String> contentList = FileUtils.readLines(tmpStore, Charset.forName("utf-8"));
            for (String content : contentList) {
                T t = JSON.parseObject(content, type);
                dataQueue.add(t);
            }
            FileUtils.deleteQuietly(tmpStore);
        } catch (Exception e) {
            logger.error("无法加载临时文件::", e);
        }
    }

    private AtomicBoolean save = new AtomicBoolean(false);

    private void save() {
        if (save.compareAndSet(false, true)) {
            logger.info("开始保存临时数据");
            try {
                List<String> stringList = new ArrayList<>(dataQueue.size());
                for (T t : dataQueue) {
                    stringList.add(JSON.toJSONString(t));
                }
                FileUtils.write(tmpStore, StringUtils.join(stringList, "\r\n"), Charset.forName("utf-8"));
                logger.info("临时数据保存OK~~~");
            } catch (IOException e) {
                logger.error("临时数据写出异常::", e);
            }
        }
    }

    @Override
    public void close() throws Exception {
        this.shutdown = true;
        try {
            thread.interrupt();
            thread.join();
        } catch (InterruptedException e) {
            logger.error("JMS处理线程退出异常::", e);
        } finally {
            save();
        }
    }

    public interface ChannelHandler<T> {
        boolean handle(T t);

        boolean available();
    }
}
