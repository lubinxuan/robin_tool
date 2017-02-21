package me.robin.utils;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * Created by xuanlubin on 2017/2/9.
 */
public class LimitFileStore {

    private static final Logger logger = LoggerFactory.getLogger(LimitFileStore.class);

    private Map<String, AtomicInteger> directoryCountMap = new ConcurrentHashMap<>();

    //默认目录大小限制512MB
    private static final long DEFAULT_LIMIT = 512 * 1024 * 1024L;

    private long limit = DEFAULT_LIMIT;

    private final NumberFormat numberFormat = new DecimalFormat("00000000");

    private final DateFormat df = new SimpleDateFormat("yyyyMMdd");

    //索引目录队列
    private final TreeSet<String> indexDirectoryQueue = new TreeSet<>(Comparator.reverseOrder());

    //文件存储根目录
    private File fileStore;

    //正在处理的数据文件过滤
    private final Map<String, Boolean> processFilter = new ConcurrentHashMap<>();

    /**
     * @param fileStore 存储根目录
     * @param limit     单目录文件大小限制
     */
    public LimitFileStore(String fileStore, long limit) throws IOException {
        this.fileStore = new File(fileStore);
        this.limit = limit;
        if (!this.fileStore.exists()) {
            FileUtils.forceMkdir(this.fileStore);
        }
        File[] indexDirectories = this.fileStore.listFiles(File::isDirectory);
        if (null != indexDirectories && indexDirectories.length > 0) {
            for (File indexDirectory : indexDirectories) {
                this.offerProcessDirectory(indexDirectory);
            }
        }
    }

    public LimitFileStore(String fileStore) throws IOException {
        this(fileStore, DEFAULT_LIMIT);
    }

    public File findFileStore(File baseDir) throws IOException {
        int id = 1;
        while (true) {
            File directory = new File(baseDir.getAbsolutePath(), getDateDirPart(id));
            boolean createStore = true;
            if (directory.exists()) {
                if (directory.isFile()) {
                    FileUtils.forceDelete(directory);
                } else {
                    createStore = false;
                }
            }
            if (createStore) {
                return directory;
            }
            if (FileUtils.sizeOfDirectory(directory) < limit) {
                return directory;
            }
            id++;
        }
    }

    public synchronized String getDateDir() {
        return df.format(new Date());
    }

    public synchronized String getDateDirPart(int id) {
        return df.format(new Date()) + "_" + numberFormat.format(id);
    }


    public int updateDirFileCount(File directory, boolean create) {
        AtomicInteger count = directoryCountMap.computeIfAbsent(directory.getAbsolutePath(), s -> new AtomicInteger(0));
        synchronized (count) {
            if (!create) {
                if (count.get() < 1) {
                    return 0;
                } else {
                    return count.decrementAndGet();
                }
            } else {
                return count.incrementAndGet();
            }
        }
    }

    public void deleteDirRecord(File directory) {
        directoryCountMap.remove(directory.getAbsolutePath());
    }

    public synchronized void offerProcessDirectory(File directory) {
        if (indexDirectoryQueue.add(directory.getName())) {
            logger.info("加入数据目录:{}", directory.getAbsolutePath());
            this.notify();
        }
    }

    /**
     * @param fileName 文件名(不要全路径) a.txt
     * @param data     要写入文件的内容
     * @return 返回当前文件存储的目录
     */
    public File writeFile(String fileName, String data) throws IOException {
        return this.writeFile(fileName, data, this.fileStore);
    }

    /**
     * @param fileName  文件名(不要全路径) a.txt
     * @param data      要写入文件的内容
     * @param fileStore 输出目录
     * @return 返回当前文件存储的目录
     */
    public File writeFile(String fileName, String data, File fileStore) throws IOException {
        File directory;
        try {
            directory = this.findFileStore(fileStore);
        } catch (IOException e) {
            logger.error("无法获取存储目录:{}", fileStore.getAbsolutePath());
            throw new RuntimeException(e);
        }
        File tmpFile = new File(fileStore, fileName);
        FileUtils.write(tmpFile, data, Charset.forName("utf-8"));
        FileUtils.moveFileToDirectory(tmpFile, directory, true);
        this.updateDirFileCount(directory, true);
        if (this.fileStore.equals(fileStore)) {
            this.offerProcessDirectory(directory);
        }
        return directory;
    }

    public void deleteDirectory(File directory) {
        String date = this.getDateDir();
        if (!directory.getName().startsWith(date)) {
            try {
                logger.info("删除空目录!!!!{}", directory.getAbsolutePath());
                FileUtils.deleteDirectory(directory);
            } catch (IOException e) {
                if (!(e instanceof FileNotFoundException)) {
                    logger.warn("目录删除异常:{}", directory.getAbsolutePath(), e);
                }
            }
        }
    }

    public void deleteStoreFile(File file) {
        try {
            FileUtils.forceDelete(file);
        } catch (IOException e) {
            if (!(e instanceof FileNotFoundException)) {
                logger.warn("文件删除异常:{}", file.getAbsolutePath(), e);
            }
        }
        File parent = file.getParentFile();

        if (parent.getName().equals(this.getDateDirPart(1))) {
            return;
        }

        int files = this.updateDirFileCount(parent, false);
        if (files == 0) {
            String[] fileArr = parent.list();
            if (null == fileArr || fileArr.length == 0) {
                this.deleteDirRecord(parent);
                try {
                    FileUtils.forceDelete(parent);
                } catch (IOException e) {
                    if (!(e instanceof FileNotFoundException)) {
                        logger.warn("目录删除异常:{}", parent.getAbsolutePath(), e);
                    }
                }
                logger.info("删除目录:{}", parent.getName());
            }
        }
    }

    public File nextProcessDirectory() throws InterruptedException {
        while (true) {
            String indexDirectoryName = indexDirectoryQueue.first();
            File indexDirectory = new File(this.fileStore, indexDirectoryName);
            if (indexDirectory.exists() && indexDirectory.isDirectory()) {
                indexDirectoryQueue.remove(indexDirectoryName);
                return indexDirectory;
            } else {
                synchronized (this) {
                    this.wait();
                }
            }
        }
    }

    public boolean checkProcessAvailable(File dataFile) {
        return processFilter.compute(dataFile.getAbsolutePath(), (k, v) -> null == v);
    }

    public void removeProcessFilter(File dataFile) {
        processFilter.remove(dataFile.getAbsolutePath());
    }

}
