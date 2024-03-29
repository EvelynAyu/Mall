package com.aining.mall.ware;

import com.alibaba.cloud.seata.GlobalTransactionAutoConfiguration;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@EnableRabbit
//@EnableTransactionManagement
@EnableDiscoveryClient
@EnableFeignClients
@SpringBootApplication(exclude = GlobalTransactionAutoConfiguration.class)
public class MallWareApplication {

    public static void main(String[] args) {
        SpringApplication.run(MallWareApplication.class, args);
    }

}
