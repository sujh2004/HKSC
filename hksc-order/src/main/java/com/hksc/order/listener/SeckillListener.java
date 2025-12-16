package com.hksc.order.listener;

import com.hksc.common.dto.SeckillMessage;
import com.hksc.order.service.OrderService; // 假设你有这个Service接口
import com.rabbitmq.client.Channel;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
public class SeckillListener {

    @Resource
    private OrderService orderService;

    /**
     * 监听秒杀队列
     * queues: 必须和 RabbitMQConfig 里定义的队列名一致
     */
    @RabbitListener(queues = "seckill.order.queue")
    public void handleSeckillOrder(SeckillMessage seckillMsg, Message message, Channel channel) {
        log.info("收到秒杀消息，准备创建订单: {}", seckillMsg);

        try {
            // 调用业务层：真正去写数据库
            orderService.createSeckillOrder(seckillMsg);

            // 如果你需要手动 ACK (确认消息已消费)，可以在这里加。
            // 默认情况下 Spring Boot 是自动 ACK 的，只要代码不报错，消息就视为消费成功。

        } catch (Exception e) {
            log.error("秒杀订单创建失败", e);
            // ⚠️ 实际生产中这里需要做“重试”或“死信”处理，防止消息丢失
        }
    }
}