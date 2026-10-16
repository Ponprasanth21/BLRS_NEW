package com.bornfire.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Dedicated executor so slow SMTP / SMS gateways never block the HTTP thread
 * that streams the reminder ZIP to the browser.
 */
@Configuration
@EnableAsync
public class BLRS_AsyncConfig {

    @Value("${notification.pool.core:4}")    private int core;
    @Value("${notification.pool.max:12}")    private int max;
    @Value("${notification.pool.queue:500}") private int queue;

    @Bean(name = "notificationExecutor")
    public Executor notificationExecutor() {
        ThreadPoolTaskExecutor ex = new ThreadPoolTaskExecutor();
        ex.setCorePoolSize(core);
        ex.setMaxPoolSize(max);
        ex.setQueueCapacity(queue);
        ex.setThreadNamePrefix("notify-");
        ex.setWaitForTasksToCompleteOnShutdown(true);
        ex.setAwaitTerminationSeconds(30);
        ex.initialize();
        return ex;
    }
}