package me.robin.hbase;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.apache.commons.collections.CollectionUtils;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.HTableInterface;
import org.apache.hadoop.hbase.client.HTablePool;
import org.apache.hadoop.hbase.client.Result;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.*;

/**
 * Created by Lubin.Xuan on 2014/12/15.
 */
public class BatchGet {
    public static List<Result> get(List<Get> getList, final String tableName, final HTablePool hTablePool) {
        List<Result> hashRet = new LinkedList<>();
        int parallel = 5;
        List<List<Get>> lstBatchKeys;
        if (getList.size() < parallel) {
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

        List<Future<Result[]>> futures = new ArrayList<>(5);

        ThreadFactoryBuilder builder = new ThreadFactoryBuilder();
        builder.setNameFormat("ParallelBatchQuery" + "-" + Thread.currentThread().getName() + "%d");
        ThreadFactory factory = builder.build();
        ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(lstBatchKeys.size(), factory);

        for (List<Get> gets : lstBatchKeys) {
            Callable<Result[]> callable = () -> {
                HTableInterface hTableInterface = hTablePool.getTable(tableName);
                try {
                    return hTableInterface.get(gets);
                } finally {
                    hTableInterface.close();
                }
            };
            FutureTask<Result[]> future = (FutureTask<Result[]>) executor.submit(callable);
            futures.add(future);
        }
        executor.shutdown();

        // Wait for all the tasks to finish
        try {
            boolean stillRunning = !executor.awaitTermination(
                    5000000, TimeUnit.MILLISECONDS);
            if (stillRunning) {
                try {
                    executor.shutdownNow();
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        } catch (InterruptedException e) {
            try {
                Thread.currentThread().interrupt();
            } catch (Exception e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
        }
        // Look for any exception
        for (Future<Result[]> f : futures) {
            try {
                if (f.get() != null) {
                    CollectionUtils.addAll(hashRet, f.get());
                }
            } catch (InterruptedException e) {
                try {
                    Thread.currentThread().interrupt();
                } catch (Exception e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
        return hashRet;
    }
}
