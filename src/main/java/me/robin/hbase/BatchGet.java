package me.robin.hbase;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import me.robin.utils.CloseableUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.hadoop.hbase.HRegionLocation;
import org.apache.hadoop.hbase.client.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.*;

/**
 * Created by Lubin.Xuan on 2014/12/15.
 */
public class BatchGet {

    private static final Logger logger = LoggerFactory.getLogger(BatchGet.class);

    public static List<Result> get2(HashMap<HRegionLocation, List<Get>> regionLocationListHashMap, final String tableName, final HTablePool hTablePool) {
        if (regionLocationListHashMap.isEmpty()) {
            return Collections.emptyList();
        }
        ThreadFactoryBuilder builder = new ThreadFactoryBuilder();
        builder.setNameFormat("HRegionLocationParallelBatchQuery" + "-" + Thread.currentThread().getName() + "%d");
        ThreadFactory factory = builder.build();
        ExecutorService executor = Executors.newFixedThreadPool(regionLocationListHashMap.size(), factory);
        List<Callable<List<Result>>> callableList = new ArrayList<>();
        for (Map.Entry<HRegionLocation, List<Get>> entry : regionLocationListHashMap.entrySet()) {
            List<Get> getList = entry.getValue();
            HRegionLocation location = entry.getKey();
            callableList.add(() -> get(location, getList, tableName, hTablePool));
        }
        try {
            List<List<Result>> resultList = exec(executor, callableList, 5000000, TimeUnit.MILLISECONDS);
            List<Result> container = new ArrayList<>();
            for (int i = 0; i < resultList.size(); i++) {
                container.addAll(resultList.get(i));
            }
            return container;
        } finally {
            regionLocationListHashMap.clear();
        }
    }

    public static List<Result> get(HRegionLocation location, List<Get> getList, final String tableName, final HTablePool hTablePool) {
        int parallel = 3;
        List<List<Get>> lstBatchKeys;
        if (getList.size() > 1000) {
            parallel = 5;
        }
        if (getList.size() < parallel * 3) {
            lstBatchKeys = new ArrayList<>(1);
            lstBatchKeys.add(new ArrayList<>(getList));
        } else {
            lstBatchKeys = new ArrayList<>(parallel);
            for (int i = 0; i < parallel; i++) {
                lstBatchKeys.add(new ArrayList<>());
            }

            int i = 0;
            for (Get k : getList) {
                lstBatchKeys.get(i % parallel).add(k);
                i++;
            }
        }

        logger.debug("RegionServer:{}_{} parallel:{} row_required:{}", location.getHostname(), location.getHostnamePort(), parallel, getList.size());

        ThreadFactoryBuilder builder = new ThreadFactoryBuilder();
        builder.setNameFormat("ParallelBatchQuery" + "-" + Thread.currentThread().getName() + "%d");
        ThreadFactory factory = builder.build();
        ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(lstBatchKeys.size(), factory);
        List<Callable<Result[]>> callableList = new ArrayList<>();
        for (List<Get> gets : lstBatchKeys) {
            Callable<Result[]> callable = () -> {
                HTableInterface hTableInterface = hTablePool.getTable(tableName);
                try {
                    return hTableInterface.get(gets);
                } finally {
                    CloseableUtils.closeQuietly(hTableInterface);
                }
            };
            callableList.add(callable);
        }
        try {
            List<Result[]> resultArray = exec(executor, callableList, 5000000, TimeUnit.MILLISECONDS);
            List<Result> container = new ArrayList<>();
            for (int i = 0; i < resultArray.size(); i++) {
                CollectionUtils.addAll(container, resultArray.get(i));
            }
            return container;
        } finally {
            getList.clear();
        }
    }

    private static <R> List<R> exec(ExecutorService executor, List<Callable<R>> callableList, long timeOut, TimeUnit timeUnit) {
        List<Future<R>> futures = new ArrayList<>();
        for (Callable<R> callable : callableList) {
            Future<R> future = executor.submit(callable);
            futures.add(future);
        }
        executor.shutdown();
        // Wait for all the tasks to finish
        try {
            boolean stillRunning = !executor.awaitTermination(timeOut, timeUnit);
            if (stillRunning) {
                try {
                    executor.shutdownNow();
                } catch (Exception e) {
                    logger.error("", e);
                }
            }
        } catch (InterruptedException e) {
            try {
                Thread.currentThread().interrupt();
            } catch (Exception e1) {
                logger.error("", e);
            }
        }
        // Look for any exception
        List<R> rList = new ArrayList<>();
        for (Future<R> f : futures) {
            try {
                if (f.get() != null) {
                    rList.add(f.get());
                }
            } catch (InterruptedException e) {
                try {
                    Thread.currentThread().interrupt();
                } catch (Exception e1) {
                    logger.error("", e);
                }
            } catch (ExecutionException e) {
                logger.error("", e);
            }
        }
        return rList;
    }
}