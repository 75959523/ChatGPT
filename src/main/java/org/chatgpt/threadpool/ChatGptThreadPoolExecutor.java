package org.chatgpt.threadpool;

import java.util.concurrent.*;

public class ChatGptThreadPoolExecutor {
    private static final int CORE_POOL_SIZE = 5;
    private static final int MAXIMUM_POOL_SIZE = 20;
    private static final int QUEUE_CAPACITY = 5;
    private static final long KEEP_ALIVE_TIME = 500L;

    private static volatile ChatGptThreadPoolExecutor instance;
    private final ThreadPoolExecutor executor;

    private ChatGptThreadPoolExecutor() {
        executor = new ThreadPoolExecutor(
                CORE_POOL_SIZE,
                MAXIMUM_POOL_SIZE,
                KEEP_ALIVE_TIME,
                TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(QUEUE_CAPACITY)
        );
        executor.setRejectedExecutionHandler(new ChatGptThreadPoolExceptionHandler(executor));
    }

    public static ChatGptThreadPoolExecutor getInstance() {
        if (instance == null) {
            synchronized (ChatGptThreadPoolExecutor.class) {
                if (instance == null) {
                    instance = new ChatGptThreadPoolExecutor();
                }
            }
        }
        return instance;
    }

    public void execute(Runnable task) {
        executor.execute(task);
    }

    public <T> Future<T> submit(Callable<T> task) {
        RunnableFuture<T> futureTask = new FutureTask<>(task);
        executor.execute(futureTask);
        return futureTask;
    }

    public void shutdown() {
        executor.shutdown();
    }
}