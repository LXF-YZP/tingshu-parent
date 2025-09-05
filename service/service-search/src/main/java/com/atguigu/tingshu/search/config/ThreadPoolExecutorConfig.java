package com.atguigu.tingshu.search.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author 陆小凤
 * @version 1.0
 * @description: TODO
 * @date 2025/9/4 17:06
 */
@Component
public class ThreadPoolExecutorConfig {


    private static final Logger log = LoggerFactory.getLogger(ThreadPoolExecutorConfig.class);

    /**
     * 自定义线程池
     * @return
     */
    @Bean
    public ThreadPoolExecutor threadPoolExecutor() {
        //自定义线程池
        //获取当前系统CPU核数
        int availabled = Runtime.getRuntime().availableProcessors();
        return new ThreadPoolExecutor(
                availabled,
                availabled,
                3,
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(100),
                Executors.defaultThreadFactory(),
                new ThreadPoolExecutor.AbortPolicy()
        );
    }
}
