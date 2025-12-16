package com.hksc.product.service.impl;

import com.hksc.common.dto.SeckillMessage;
import com.hksc.common.result.Result;
import com.hksc.product.service.SeckillService;
import jakarta.annotation.Resource;
import org.springframework.amqp.rabbit.core.RabbitTemplate; // 确保是这个包
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.UUID;

@Service
public class SeckillServiceImpl implements SeckillService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private RabbitTemplate rabbitTemplate;

    @Resource
    private DefaultRedisScript<Long> seckillScript;

    @Override
    public Result<String> seckill(Long skuId, Long userId) {
        // 1. 生成排队凭证
        String orderToken = UUID.randomUUID().toString();

        // 2. 执行 Lua 脚本 (确保脚本第一行是 local seckillId = KEYS[1])
        Long result = stringRedisTemplate.execute(
                seckillScript,
                Collections.singletonList(skuId.toString()), // KEYS
                userId.toString()                            // ARGV
        );

        // 3. 判空处理
        if (result == null) {
            return Result.error("系统繁忙"); // 修正：使用 error
        }

        // 4. 根据返回值处理
        switch (result.intValue()) {
            case -1:
                return Result.error("秒杀未开始 (库存未预热)"); // 修正：使用 error
            case 0:
                return Result.error("手慢了，库存已抢光");     // 修正：使用 error
            case 2:
                return Result.error("您已购买过，请勿重复抢购"); // 修正：使用 error
            case 1:
                // 5. 发送消息到 RabbitMQ
                SeckillMessage message = new SeckillMessage(userId, skuId, orderToken);

                // 发送的核心代码
                // 如果这里还报错，请看下方的“排错指南”
                rabbitTemplate.convertAndSend("seckill.direct", "seckill.order", message);

                return Result.success(orderToken);
            default:
                return Result.error("未知错误");
        }
    }
}