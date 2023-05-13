package org.chatgpt;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.FeignClientProperties;

@SpringBootApplication
@EnableFeignClients(defaultConfiguration = {FeignClientProperties.FeignClientConfiguration.class})
public class Run {

    public static void main(String[] args) {
        SpringApplication.run(Run.class, args);
    }
}
