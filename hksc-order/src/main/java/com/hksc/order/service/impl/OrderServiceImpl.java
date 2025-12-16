package com.hksc.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.hksc.common.dto.SeckillMessage;
import com.hksc.common.result.Result;
import com.hksc.order.dto.OrderCreateDTO;
import com.hksc.order.dto.ProductDTO;
import com.hksc.order.entity.OrderInfo;
import com.hksc.order.entity.OrderItem;
import com.hksc.order.entity.SeckillSku;
import com.hksc.order.feign.ProductClient;
import com.hksc.order.mapper.OrderInfoMapper;
import com.hksc.order.mapper.OrderItemMapper;
import com.hksc.order.mapper.SeckillSkuMapper;
import com.hksc.order.service.OrderService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID; // 引入 UUID 生成普通订单号

@Slf4j
@Service
public class OrderServiceImpl implements OrderService {

    @Resource
    private OrderInfoMapper orderMapper;
    @Resource
    private OrderItemMapper itemMapper;

    @Resource
    private SeckillSkuMapper seckillSkuMapper; // 需要这个Mapper来扣数据库库存

    @Resource
    private ProductClient productClient; // 远程调用
    @Resource
    private RabbitTemplate rabbitTemplate; // 消息队列

    // ================== 1. 普通下单  ==================
    @Override
    @Transactional(rollbackFor = Exception.class) // 本地事务
    public Result<String> createOrder(Long userId, OrderCreateDTO dto) {
        // 1. 远程调用商品服务：扣减库存
        Result<Boolean> stockResult = productClient.deductStock(dto.getProductId(), dto.getCount());
        if (stockResult.getCode() != 200) {
            return Result.error("库存扣减失败: " + stockResult.getMessage());
        }

        // 2. 获取商品信息 (获取价格)
        Result<ProductDTO> productResult = productClient.getProduct(dto.getProductId());
        if (productResult == null || productResult.getData() == null) {
            return Result.error("商品不存在");
        }
        BigDecimal price = productResult.getData().getPrice();
        String productName = productResult.getData().getTitle();

        // 生成一个普通订单的订单号
        String orderSn = "ORD-" + System.currentTimeMillis();

        // 3. 创建主订单 (OrderInfo)
        OrderInfo order = new OrderInfo();
        order.setUserId(userId);
        order.setOrderSn(orderSn); // 订单号
        order.setStatus(0); // 待付款
        order.setCreateTime(new Date()); // 创建时间

        // 计算金额
        BigDecimal totalAmount = price.multiply(BigDecimal.valueOf(dto.getCount()));
        order.setTotalAmount(totalAmount);
        order.setPayAmount(totalAmount); // 实付金额
        order.setNote("普通下单");       // 备注

        orderMapper.insert(order); // MyBatisPlus 会自动回填 ID 到 order 对象

        // 4. 创建订单明细 (OrderItem)
        OrderItem item = new OrderItem();
        item.setOrderId(order.getId());
        item.setOrderSn(orderSn);      // 关联订单号
        item.setProductId(dto.getProductId());


        item.setProductPrice(price); // 对应 productPrice 字段

        item.setBuyCount(dto.getCount());    // 对应 buyCount 字段

        item.setProductName(productName != null ? productName : "普通商品-" + dto.getProductId());

        itemMapper.insert(item);

        // 5. 发送消息到 RabbitMQ (通知购物车服务等)
        String msg = userId + ":" + dto.getProductId();
        rabbitTemplate.convertAndSend("order.created.queue", msg);
        // 发送延迟消息用于超时取消
        rabbitTemplate.convertAndSend("", "order.delay.queue", order.getId().toString());

        return Result.success(order.getId().toString());
    }

    // ================== 2. 秒杀下单  ==================
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createSeckillOrder(SeckillMessage msg) {
        Long skuId = msg.getSkuId();     // 秒杀商品ID
        Long userId = msg.getUserId();   // 用户ID
        String orderSn = msg.getOrderToken(); // 订单号

        log.info("秒杀落库开始 -> User:{}, SKU:{}", userId, skuId);

        // 1. 扣减库存 (操作 hksc_order 库里的 seckill_sku 表)
        boolean updateResult = seckillSkuMapper.update(null, new UpdateWrapper<SeckillSku>()
                .setSql("seckill_stock = seckill_stock - 1")
                .eq("id", skuId)
                .gt("seckill_stock", 0)
        ) > 0;

        if (!updateResult) {
            log.error("库存扣减失败(卖完了): {}", skuId);
            throw new RuntimeException("库存不足");
        }

        // 2. 查价格
        SeckillSku skuInfo = seckillSkuMapper.selectById(skuId);
        if (skuInfo == null) {
            throw new RuntimeException("商品不存在");
        }

        // 3. 插入主订单表
        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setUserId(userId);
        orderInfo.setOrderSn(orderSn);
        orderInfo.setTotalAmount(skuInfo.getSeckillPrice());
        orderInfo.setPayAmount(skuInfo.getSeckillPrice());
        orderInfo.setStatus(0); // 待付款
        orderInfo.setCreateTime(new Date());
        orderInfo.setNote("秒杀订单");

        orderMapper.insert(orderInfo);

        // 4. 插入订单明细表
        OrderItem orderItem = new OrderItem();
        orderItem.setOrderId(orderInfo.getId());
        orderItem.setOrderSn(orderSn);
        orderItem.setSkuId(skuInfo.getId());
        orderItem.setProductId(skuInfo.getProductId());
        orderItem.setProductPrice(skuInfo.getSeckillPrice());
        orderItem.setBuyCount(1);
        orderItem.setProductName("秒杀活动商品");

        itemMapper.insert(orderItem);

        log.info("秒杀订单完成: {}", orderInfo.getId());
    }
}