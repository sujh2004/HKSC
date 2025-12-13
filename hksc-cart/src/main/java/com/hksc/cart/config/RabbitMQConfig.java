package com.hksc.cart.config;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    // Cart 服务也声明一下，这样谁先启动都能把队列建出来，不用互相等
    @Bean
    public Queue orderCreatedQueue() {
        return new Queue("order.created.queue", true); // true 表示持久化
    }
}