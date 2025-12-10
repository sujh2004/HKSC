package com.hksc.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * HKSC 认证中心启动类
 */
@SpringBootApplication(
        // 【添加这个排除】 禁用数据库自动配置
        exclude = {DataSourceAutoConfiguration.class}
)
@EnableDiscoveryClient // 启用服务发现，将自己注册到 Nacos
public class HkscAuthApplication {
    public static void main(String[] args) {
        SpringApplication.run(HkscAuthApplication.class, args);
    }
}