package com.hksc.order.service;

import com.hksc.common.dto.SeckillMessage;
import com.hksc.common.result.Result;
import com.hksc.order.dto.OrderCreateDTO;
import com.hksc.order.vo.OrderDetailVO;
import com.hksc.order.vo.OrderVO;
import com.hksc.order.vo.SeckillStatusVO;

import java.util.List;

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

    /**
     * 轮询秒杀是否成功
     */
    Result<String> getSeckillOrderStatus(Long userId, Long skuId);

    /**
     * 查询订单详情（返回VO）
     * @param orderId 订单ID
     * @param userId 用户ID（用于权限校验）
     * @return 订单详情VO
     */
    Result<OrderDetailVO> getOrderDetail(Long orderId, Long userId);

    /**
     * 查询用户订单列表（返回VO）
     * @param userId 用户ID
     * @return 订单列表VO
     */
    Result<List<OrderVO>> getOrderList(Long userId);

    /**
     * 模拟支付订单（简化版，无真实支付对接）
     * @param orderId 订单ID
     * @param userId 用户ID（权限校验）
     * @return 支付结果
     */
    Result<String> payOrder(Long orderId, Long userId);
}