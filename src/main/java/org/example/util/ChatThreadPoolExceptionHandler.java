package org.example.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

public class ChatThreadPoolExceptionHandler implements RejectedExecutionHandler {

    private static final Logger logger = LoggerFactory.getLogger(ChatThreadPoolExceptionHandler.class);

    @Override
    public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
        logger.info("最大线程数已满，自定义拒绝线程任务 " + r.getClass());
        //TODO
    }

}
