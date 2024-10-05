package com.gmitaros.vesselmetrics;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@SpringBootApplication
public class VesselMetricsApplication {

    public static void main(String[] args) {
        SpringApplication.run(VesselMetricsApplication.class, args);
    }

    @Bean
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(4);
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("GithubLookup-");
        executor.initialize();
        return executor;
    }

}
