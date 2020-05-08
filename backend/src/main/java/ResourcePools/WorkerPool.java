package ResourcePools;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;

public class WorkerPool {
    private ThreadPoolExecutor executor;
    private static final int MAX_POOL_SIZE = 10;


    public WorkerPool() {

        ThreadFactory threadFactory = Executors.defaultThreadFactory();
        executor = (ThreadPoolExecutor) Executors.newCachedThreadPool();
//        executor.setMaximumPoolSize(MAX_POOL_SIZE);

//        RejectedExecutionHandlerImpl rejectionHandler = new RejectedExecutionHandlerImpl();
//        executor.setRejectedExecutionHandler(rejectionHandler);
    }

    public void shutdown() {
        executor.shutdown();
    }

    public void execute(Runnable r) {
        System.out.println("WORKER POOL EXEC");
        executor.execute(r);
    }

    public void submit(Runnable r) {
        executor.submit(r);
    }
}