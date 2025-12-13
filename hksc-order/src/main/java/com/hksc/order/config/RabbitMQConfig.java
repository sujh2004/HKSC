package com.hksc.order.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class RabbitMQConfig {

    // 1. 之前定义的下单通知队列 (保持不变)
    @Bean
    public Queue orderCreatedQueue() {
        return new Queue("order.created.queue", true);
    }

    // ================== 新增：延迟队列配置 ==================

    // 2. 定义死信交换机 (DLX)
    @Bean
    public DirectExchange orderDlxExchange() {
        return new DirectExchange("order.dlx.exchange");
    }

    // 3. 定义死信队列 (真正处理取消逻辑的队列)
    @Bean
    public Queue orderDlxQueue() {
        return new Queue("order.dlx.queue", true);
    }

    // 4. 绑定死信队列到死信交换机
    @Bean
    public Binding bindingDlx() {
        return BindingBuilder.bind(orderDlxQueue())
                .to(orderDlxExchange())
                .with("order.cancel"); // Routing Key
    }

    // 5. 定义延迟队列 (消息发给它，它不消费，等过期)
    @Bean
    public Queue orderDelayQueue() {
        Map<String, Object> args = new HashMap<>();
        // 核心配置：消息过期后，转发给哪个交换机？
        args.put("x-dead-letter-exchange", "order.dlx.exchange");
        // 核心配置：转发时的 routing key 是什么？
        args.put("x-dead-letter-routing-key", "order.cancel");
        // 核心配置：统一过期时间 (比如 30分钟，这里为了测试设为 1分钟 = 60000ms)
        args.put("x-message-ttl", 60000);

        return QueueBuilder.durable("order.delay.queue")
                .withArguments(args)
                .build();
    }
}