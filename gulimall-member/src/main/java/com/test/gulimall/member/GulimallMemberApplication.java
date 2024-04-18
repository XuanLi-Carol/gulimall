package com.test.gulimall.member;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/***
 * OpenFeign使用步骤：
 * 1，导入open-feign依赖
 * 2，编写一个接口，告诉spring cloud接口需要调用远程服务
 *     1) 声明接口的每一个方法都是调用远程服务的哪个API
 * 3， 启用远程调用    功能
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.test.gulimall.member.feign")
public class GulimallMemberApplication {

    public static void main(String[] args) {
        SpringApplication.run(GulimallMemberApplication.class, args);
    }

}
