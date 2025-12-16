package com.hksc.order.service;

import com.hksc.common.dto.SeckillMessage;
import com.hksc.common.result.Result;
import com.hksc.order.dto.OrderCreateDTO;

public interface OrderService {

    /**
     * 创建订单
     * @param userId 用户ID
     * @param dto 下单参数
     * @return 订单ID
     */
    Result<String> createOrder(Long userId, OrderCreateDTO dto);
    /**
     * 秒杀异步下单接口 (对应 RabbitMQ 监听器)
     * 注意：这里不需要返回 Result，因为是 MQ 异步调用的
     */
    void createSeckillOrder(SeckillMessage msg);
}