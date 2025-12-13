package com.hksc.order.service.impl;

import com.hksc.common.result.Result;
import com.hksc.order.dto.OrderCreateDTO;
import com.hksc.order.dto.ProductDTO;
import com.hksc.order.entity.OrderInfo;
import com.hksc.order.entity.OrderItem;
import com.hksc.order.feign.ProductClient;
import com.hksc.order.mapper.OrderInfoMapper;
import com.hksc.order.mapper.OrderItemMapper;
import com.hksc.order.service.OrderService;
import jakarta.annotation.Resource;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class OrderServiceImpl implements OrderService {

    @Resource
    private OrderInfoMapper orderMapper;
    @Resource
    private OrderItemMapper itemMapper;
    @Resource
    private ProductClient productClient; // 远程调用
    @Resource
    private RabbitTemplate rabbitTemplate; // 消息队列

    @Override
    @Transactional(rollbackFor = Exception.class) // 本地事务
    public Result<String> createOrder(Long userId, OrderCreateDTO dto) {
        // 1. 远程调用商品服务：扣减库存 (这是最简单的同步调用，后续可优化为分布式事务 Seata)
        Result<Boolean> stockResult = productClient.deductStock(dto.getProductId(), dto.getCount());
        if (stockResult.getCode() != 200) {
            return Result.error("库存扣减失败: " + stockResult.getMessage());
        }

        // 2. 创建主订单
        OrderInfo order = new OrderInfo();
        order.setUserId(userId);
        order.setStatus(0); // 待付款
        Result<ProductDTO> productResult = productClient.getProduct(dto.getProductId());
        BigDecimal price = productResult.getData().getPrice();
        order.setTotalAmount(price.multiply(BigDecimal.valueOf(dto.getCount())));
        orderMapper.insert(order); // MyBatisPlus 会自动回填 ID 到 order 对象

        // 3. 创建订单明细
        OrderItem item = new OrderItem();
        item.setOrderId(order.getId());
        item.setProductId(dto.getProductId());
        item.setPrice(price);
        item.setQuantity(dto.getCount());
        itemMapper.insert(item);

        // 4. 发送消息到 RabbitMQ (通知购物车服务：这个用户买了东西，请清空对应购物车)
        // 消息格式： "userId:productId"
        String msg = userId + ":" + dto.getProductId();
        rabbitTemplate.convertAndSend("order.created.queue", msg);
        rabbitTemplate.convertAndSend("", "order.delay.queue", order.getId().toString());

        return Result.success(order.getId().toString());
    }
}