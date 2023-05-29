package org.chatgpt.threadpool;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;

public class ChatGptThreadPoolExceptionHandler implements RejectedExecutionHandler {

    private static final Logger logger = LoggerFactory.getLogger(ChatGptThreadPoolExceptionHandler.class);

    private final BlockingQueue<RunnableFuture<?>> buffer;
    private final ThreadPoolExecutor threadPoolExecutor;

    public ChatGptThreadPoolExceptionHandler(ThreadPoolExecutor threadPoolExecutor) {
        this.buffer = new LinkedBlockingQueue<>();
        this.threadPoolExecutor = threadPoolExecutor;
        startBufferConsumerThread();
    }

    @Override
    public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
        if (!(r instanceof RunnableFuture)) {
            throw new IllegalArgumentException("Task must be of type RunnableFuture");
        }
        RunnableFuture<?> futureTask = (RunnableFuture<?>) r;
        logger.info("The maximum number of threads is full, put the task into the buffer queue" + r.getClass());
        try {
            buffer.put(futureTask);
        } catch (InterruptedException e) {
            logger.error("Failed to put task into buffer queue" + r.getClass(), e);
        }
    }

    private void startBufferConsumerThread() {
        new Thread(() -> {
            while (true) {
                try {
                    RunnableFuture<?> task = buffer.take();
                    //If the thread pool is not closed, try to resubmit the task to the thread pool
                    threadPoolExecutor.execute(task);
                } catch (InterruptedException e) {
                    logger.error("Failed to get task from buffer queue", e);
                }
            }
        }).start();
    }
}