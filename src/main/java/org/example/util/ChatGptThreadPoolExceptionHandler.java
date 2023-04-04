package org.example.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

public class ChatGptThreadPoolExceptionHandler implements RejectedExecutionHandler {

    private static final Logger logger = LoggerFactory.getLogger(ChatGptThreadPoolExceptionHandler.class);

    private final BlockingQueue<Runnable> buffer;
    private final ThreadPoolExecutor threadPoolExecutor;

    public ChatGptThreadPoolExceptionHandler(ThreadPoolExecutor threadPoolExecutor) {
        this.buffer = new LinkedBlockingQueue<>();
        this.threadPoolExecutor = threadPoolExecutor;
        startBufferConsumerThread();
    }

    @Override
    public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
        logger.info("最大线程数已满，将任务放入缓冲队列 " + r.getClass());
        try {
            buffer.put(r);
        } catch (InterruptedException e) {
            logger.error("任务放入缓冲队列失败 " + r.getClass(), e);
        }
    }

    private void startBufferConsumerThread() {
        new Thread(() -> {
            while (true) {
                try {
                    Runnable task = buffer.take();
                    // 如果线程池未关闭，尝试将任务重新提交到线程池。
                    threadPoolExecutor.execute(task);
                } catch (InterruptedException e) {
                    logger.error("从缓冲队列中获取任务失败", e);
                }
            }
        }).start();
    }
}