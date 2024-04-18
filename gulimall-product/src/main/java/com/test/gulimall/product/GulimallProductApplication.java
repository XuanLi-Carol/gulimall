package com.test.gulimall.product;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/*@MapperScan("com.test.gulimall.product.dao")*/
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients(basePackages = {"com.test.gulimall.product.service.feign"})
public class GulimallProductApplication {

    public static void main(String[] args) {
        SpringApplication.run(GulimallProductApplication.class, args);
    }

}