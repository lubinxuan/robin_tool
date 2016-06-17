package me.robin.jms;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.monitor.FileAlterationListener;
import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by xuanlubin on 2016/6/8.
 */
public class JMSPersist {

    private static final Logger logger = LoggerFactory.getLogger(JMSPersist.class);

    private static final String FILE_SUFFIX = ".jms";

    private static final String DEFAULT_TMP_STORE_PATH = "./jms_tmp";

    private File tmpStoreDir;

    private final Queue<File> fileQueue = new LinkedList<>();

    private ExecutorService service;

    private JMSHandler jmsHandler;

    public JMSPersist(JMSHandler jmsHandler, String fileDir) {
        this.jmsHandler = jmsHandler;
        init(fileDir);
    }


    public JMSPersist(JMSHandler jmsHandler) {
        this(jmsHandler, "./jms_tmp");
    }

    public void setService(ExecutorService service) {
        this.service = service;
    }

    private void createTmpDir(String fileDir) {

        if (StringUtils.isBlank(fileDir)) {
            fileDir = DEFAULT_TMP_STORE_PATH;
        }

        tmpStoreDir = new File(fileDir.trim());
        if (!tmpStoreDir.exists() || tmpStoreDir.isFile()) {
            tmpStoreDir.mkdirs();
        }
    }

    private void init(String store) {
        //创建文件存放目录
        createTmpDir(store);

        FileAlterationObserver observer = new FileAlterationObserver(tmpStoreDir, file -> file.isFile() && file.getName().endsWith(FILE_SUFFIX));
        FileAlterationListener listener = new FileAlterationListenerAdaptor() {
            @Override
            public void onFileCreate(File file) {
                offer(file);
            }
        };

        File[] listFiles = observer.getDirectory().listFiles(observer.getFileFilter());
        if (null != listFiles && listFiles.length > 0) {
            for (File file : listFiles) {
                offer(file);
            }
        }

        observer.addListener(listener);

        try {
            new FileAlterationMonitor(1000, observer).start();
        } catch (Throwable e) {
            throw new RuntimeException("异常文件提交监听线程启动失败");
        }


        new Timer("JMS_FILE_HANDLER_" + tmpStoreDir.getAbsolutePath()).schedule(new TimerTask() {
            @Override
            public void run() {
                while (true) {

                    if (!jmsHandler.available()) {
                        try {
                            TimeUnit.SECONDS.sleep(5);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    File file = fileQueue.poll();
                    if (null == file) {
                        break;
                    }
                    logger.debug("开始处理:{}", file);
                    String fileContent = null;
                    try {
                        fileContent = IOUtils.toString(file.toURI(), "utf-8");
                    } catch (IOException e) {
                        logger.error("fileContent读取异常! " + file.getAbsolutePath(), e);
                        return;
                    }

                    if (StringUtils.isBlank(fileContent)) {
                        return;
                    }

                    if (null != service) {
                        String _fileContent = fileContent;
                        service.execute(() -> exec(_fileContent, file));
                    } else {
                        exec(fileContent, file);
                    }


                }
            }
        }, 0, 1000);

    }

    private void exec(String fileContent, File file) {
        long start = System.currentTimeMillis();
        if (jmsHandler.handle(fileContent)) {
            logger.debug("文件处理完成:{}    {}", file, System.currentTimeMillis() - start);
            file.delete();
        } else {
            logger.info("文件处理异常，重新入列");
            fileQueue.offer(file);
        }
    }

    private void offer(File file) {
        fileQueue.offer(file);
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
        String filePath = "jms_" + date + "_" + id.incrementAndGet() + ".tmp";
        File back = new File(tmpStoreDir, filePath);
        try {
            OutputStream os = new FileOutputStream(back);
            IOUtils.write(fileContent, os, "utf-8");
            IOUtils.closeQuietly(os);
            back.renameTo(new File(tmpStoreDir, filePath.replace(".tmp", FILE_SUFFIX)));
        } catch (Throwable r) {
            logger.error("文件写出异常~~~", r);
        }
    }
}
