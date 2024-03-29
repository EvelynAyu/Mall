package com.aining.mall.member;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

@EnableRedisHttpSession
@EnableFeignClients(basePackages = "com.aining.mall.member.feign")
@SpringBootApplication
@EnableDiscoveryClient
public class MallMemberApplication {
    public static void main(String[] args){
        SpringApplication.run(MallMemberApplication.class,args);
    }
}
