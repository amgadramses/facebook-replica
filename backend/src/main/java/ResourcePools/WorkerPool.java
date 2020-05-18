package ResourcePools;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;

public class WorkerPool {
    private ThreadPoolExecutor executor;
    private static final int MAX_POOL_SIZE = 10;


    public WorkerPool() {
        executor = (ThreadPoolExecutor) Executors.newCachedThreadPool();

    }

    public void shutdown() {
        executor.shutdown();
    }

    public void execute(Runnable r) {

        executor.execute(r);
    }

    public void submit(Runnable r) {
        executor.submit(r);
    }
}