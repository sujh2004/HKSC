package com.hksc.cart.listener;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class OrderListener {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    private static final String CART_PREFIX = "cart:user:";

    @RabbitListener(queues = "order.created.queue")
    public void clearCartItem(String message) {
        // 1. 校验消息
        if (message == null || !message.contains(":")) {
            System.err.println("消息格式错误，忽略: " + message);
            return;
        }

        try {
            // 2. 解析消息 "userId:productId"
            String[] parts = message.split(":");
            String userId = parts[0];
            String productId = parts[1];

            String key = CART_PREFIX + userId;

            // 4. 执行删除
            // 对应 addToCart 里的: opsForHash().put(key, productId.toString(), ...)
            // 这里执行 delete(key, hashKey)
            stringRedisTemplate.opsForHash().delete(key, productId);

            System.out.println("购物车清理成功 - 用户: " + userId + ", 商品: " + productId);

        } catch (Exception e) {
            System.err.println("处理购物车清理消息失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
}