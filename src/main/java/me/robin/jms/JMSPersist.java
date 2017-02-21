package me.robin.jms;

import me.robin.utils.LimitFileStore;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;

/**
 * Created by xuanlubin on 2016/6/8.
 */
public class JMSPersist<T> implements Closeable {

    private static final Logger logger = LoggerFactory.getLogger(JMSPersist.class);

    private static final String FILE_SUFFIX = ".jms";

    private static final String DEFAULT_TMP_STORE_PATH = "./jms_tmp";

    private ThreadPoolExecutor service;

    private final JMSHandler<T> jmsHandler;
    private final Function<String, T> dataConverter;
    private Map<File, T> bufferMap = new ConcurrentHashMap<>();
    private Thread thread;

    private boolean shutdown = false;

    private final LimitFileStore limitFileStore;

    public JMSPersist(Function<String, T> dataConverter, JMSHandler<T> jmsHandler, String fileDir) {
        this.jmsHandler = jmsHandler;
        this.dataConverter = dataConverter;
        try {
            this.limitFileStore = new LimitFileStore(StringUtils.isBlank(fileDir) ? DEFAULT_TMP_STORE_PATH : fileDir);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        init();
    }


    public JMSPersist(Function<String, T> dataConverter, JMSHandler<T> jmsHandler) {
        this(dataConverter, jmsHandler, DEFAULT_TMP_STORE_PATH);
    }

    public void setService(ThreadPoolExecutor service) {
        this.service = service;
    }

    private void init() {
        Runnable runnable = () -> {
            loop:
            while (!shutdown) {
                File directory;

                try {
                    directory = limitFileStore.nextProcessDirectory();
                } catch (InterruptedException e) {
                    continue;
                }

                File[] dataFiles = directory.listFiles(file -> file.isFile() && file.getName().endsWith(FILE_SUFFIX));
                logger.info("开始处理数据目录:{}", directory.getAbsolutePath());
                if (null != dataFiles && dataFiles.length > 0) {
                    for (File dataFile : dataFiles) {
                        if (null != service) {
                            if (limitFileStore.checkProcessAvailable(dataFile)) {
                                service.execute(() -> {
                                    try {
                                        exec(dataFile);
                                    } finally {
                                        synchronized (jmsHandler) {
                                            jmsHandler.notify();
                                        }
                                        this.limitFileStore.removeProcessFilter(dataFile);
                                    }
                                });
                                if (!jmsHandler.available()) {
                                    synchronized (jmsHandler) {
                                        try {
                                            jmsHandler.wait();
                                        } catch (InterruptedException e) {
                                            continue loop;
                                        }
                                    }
                                }
                            }
                        } else {
                            exec(dataFile);
                        }
                    }
                } else {
                    this.limitFileStore.deleteDirectory(directory);
                }
            }
            if (null != service) {
                service.shutdownNow();
            }
            logger.info("JMS任务调度线程退出!!!!");
        };

        thread = new Thread(runnable);
        thread.setName("jms_file_schedule_thread");
        thread.start();
    }

    private void exec(File file) {
        String fileContent;
        try {
            fileContent = IOUtils.toString(file.toURI(), "utf-8");
        } catch (IOException e) {
            logger.error("fileContent读取异常! " + file.getAbsolutePath(), e);
            return;
        }

        if (StringUtils.isBlank(fileContent)) {
            this.limitFileStore.deleteStoreFile(file);
            return;
        }
        logger.info("开始处理:{}", file.getAbsolutePath());
        T data = dataConverter.apply(fileContent);
        long start = System.currentTimeMillis();
        if (jmsHandler.handle(data)) {
            logger.debug("文件处理完成:{}    {}", file, System.currentTimeMillis() - start);
            this.limitFileStore.deleteStoreFile(file);
        } else {
            logger.info("文件处理异常，重新入列");
        }
    }

    private static final DateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
    private AtomicLong id = new AtomicLong();

    public void writeFile(String fileContent) {

        if (StringUtils.isBlank(fileContent)) {
            return;
        }

        String date;
        synchronized (df) {
            date = df.format(new Date());
        }
        String filePath = "jms_" + date + "_" + id.incrementAndGet() + ".jms";
        try {
            this.limitFileStore.writeFile(filePath, fileContent);
        } catch (Throwable r) {
            logger.error("文件写出异常~~~", r);
        }
    }

    @Override
    public void close() throws IOException {
        thread.interrupt();
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
