package com.hksc.order.config;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    // 定义一个队列，下单成功后发送消息，将来购物车服务监听这个队列来清空购物车
    @Bean
    public Queue orderCreatedQueue() {
        return new Queue("order.created.queue", true);
    }
}