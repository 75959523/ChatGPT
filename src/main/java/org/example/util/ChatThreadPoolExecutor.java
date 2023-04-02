package org.example.util;


import java.util.concurrent.*;

public class ChatThreadPoolExecutor {
    public static ExecutorService newFixedThreadPool(int core, int max, int queue){
        return new ThreadPoolExecutor(
                core,
                max,
                500L,
                TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(queue),
                (RejectedExecutionHandler)new ChatThreadPoolExceptionHandler()
        );
    }
}
