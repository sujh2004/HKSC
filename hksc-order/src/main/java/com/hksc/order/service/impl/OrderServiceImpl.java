package com.hksc.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.hksc.common.dto.SeckillMessage;
import com.hksc.common.result.Result;
import com.hksc.order.converter.OrderConverter;
import com.hksc.order.dto.OrderCreateDTO;
import com.hksc.order.dto.ProductDTO;
import com.hksc.order.entity.OrderInfo;
import com.hksc.order.entity.OrderItem;
import com.hksc.order.entity.SeckillStock;
import com.hksc.order.feign.CartClient;
import com.hksc.order.feign.ProductClient;
import com.hksc.order.mapper.OrderInfoMapper;
import com.hksc.order.mapper.OrderItemMapper;
import com.hksc.order.mapper.SeckillStockMapper;
import com.hksc.order.service.OrderService;
import com.hksc.order.vo.OrderDetailVO;
import com.hksc.order.vo.OrderVO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class OrderServiceImpl implements OrderService {

    @Resource
    private OrderInfoMapper orderMapper;
    @Resource
    private OrderItemMapper itemMapper;

    @Resource
    private SeckillStockMapper seckillStockMapper; // 需要这个Mapper来扣数据库库存

    @Resource
    private ProductClient productClient; // 远程调用

    @Resource
    private CartClient cartClient; // 注入刚才写的客户端

    @Resource
    private RabbitTemplate rabbitTemplate; // 消息队列

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    // ================== 1. 普通下单  ==================
    @Override
    @Transactional(rollbackFor = Exception.class) // 本地事务
    public Result<String> createOrder(Long userId, OrderCreateDTO dto) {
        log.info("=== 开始创建订单 === userId:{}, productId:{}, count:{}", userId, dto.getProductId(), dto.getCount());

        // 1. 远程调用商品服务：扣减库存
        log.info("【步骤1】调用商品服务扣减库存...");
        Result<Boolean> stockResult = null;
        try {
            stockResult = productClient.deductStock(dto.getProductId(), dto.getCount());
            log.info("【步骤1】库存扣减调用完成，返回结果: code={}, message={}, data={}",
                    stockResult != null ? stockResult.getCode() : "null",
                    stockResult != null ? stockResult.getMessage() : "null",
                    stockResult != null ? stockResult.getData() : "null");
        } catch (Exception e) {
            log.error("【步骤1】库存扣减调用异常: {}", e.getMessage(), e);
            return Result.error("库存扣减失败: " + e.getMessage());
        }

        if (stockResult == null) {
            log.error("【步骤1】库存扣减返回null");
            return Result.error("库存扣减失败: 服务无响应");
        }

        if (stockResult.getCode() != 200) {
            log.error("【步骤1】库存扣减失败: code={}, message={}", stockResult.getCode(), stockResult.getMessage());
            return Result.error("库存扣减失败: " + stockResult.getMessage());
        }

        log.info("【步骤1】库存扣减成功");

        // 2. 获取商品信息 (获取价格)
        log.info("【步骤2】获取商品信息...");
        Result<ProductDTO> productResult = productClient.getProduct(dto.getProductId());
        if (productResult == null || productResult.getData() == null) {
            log.error("【步骤2】商品不存在");
            return Result.error("商品不存在");
        }
        BigDecimal price = productResult.getData().getPrice();
        String productName = productResult.getData().getTitle();
        log.info("【步骤2】商品信息获取成功: name={}, price={}", productName, price);

        // 生成一个普通订单的订单号
        String orderSn = "ORD-" + System.currentTimeMillis();

        // 3. 创建主订单 (OrderInfo)
        log.info("【步骤3】创建订单主表...");
        OrderInfo order = new OrderInfo();
        order.setUserId(userId);
        order.setOrderSn(orderSn); // 订单号
        order.setStatus(0); // 待付款
        order.setCreateTime(LocalDateTime.now()); // 创建时间

        // 计算金额
        BigDecimal totalAmount = price.multiply(BigDecimal.valueOf(dto.getCount()));
        order.setTotalAmount(totalAmount);
        order.setPayAmount(totalAmount); // 实付金额
        order.setNote("普通下单");       // 备注

        orderMapper.insert(order); // MyBatisPlus 会自动回填 ID 到 order 对象
        log.info("【步骤3】订单主表创建成功: orderId={}, orderSn={}", order.getId(), orderSn);

        // 4. 创建订单明细 (OrderItem)
        log.info("【步骤4】创建订单明细...");
        OrderItem item = new OrderItem();
        item.setOrderId(order.getId());
        item.setProductId(dto.getProductId());


        item.setProductPrice(price); // 对应 productPrice 字段

        item.setBuyCount(dto.getCount());    // 对应 buyCount 字段

        item.setProductName(productName != null ? productName : "普通商品-" + dto.getProductId());

        itemMapper.insert(item);
        log.info("【步骤4】订单明细创建成功: itemId={}", item.getId());


        // 清理购物车
        // 只有当 dto 是从购物车提交过来的时候才需要删
        // 假设 dto.getSkuId() 是用户买的商品，如果是购物车结算，可能是一批 ID
        try {
            log.info("【步骤5】清理购物车...");
            // 这里为了演示，把当前买的这个商品 ID 放到 List 里传过去
            List<Long> skuIds = java.util.Collections.singletonList(dto.getProductId());

            // 远程调用！如果购物车挂了，会走 Fallback 返回 true，不会报错
            cartClient.deleteChecked(userId, skuIds);

            log.info("【步骤5】调用购物车清理结束");
        } catch (Exception e) {
            // 这一层 try-catch 是双重保险，防止 Fallback 配置失败导致影响下单
            log.warn("【步骤5】清理购物车失败，但不影响下单: {}", e.getMessage());
        }


        // 5. 发送消息到 RabbitMQ (通知购物车服务等)
        log.info("【步骤6】发送MQ消息...");
        String msg = userId + ":" + dto.getProductId();
        rabbitTemplate.convertAndSend("order.created.queue", msg);

        // 发送延迟消息用于超时取消（消息级别TTL：30分钟）
        rabbitTemplate.convertAndSend("", "order.delay.queue", order.getId().toString(), message -> {
            // ✅ 优化：设置消息级别TTL（30分钟 = 30 * 60 * 1000）
            message.getMessageProperties().setExpiration("1800000");  // 30分钟
            return message;
        });
        log.info("【步骤6】MQ消息发送成功");

        log.info("=== 订单创建完成 === orderId:{}", order.getId());
        return Result.success(order.getId().toString());
    }

    // ================== 2. 秒杀下单  ==================
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createSeckillOrder(SeckillMessage msg) {
        Long productId = msg.getSkuId();  // 这里实际上是商品ID，不是秒杀活动ID
        Long userId = msg.getUserId();    // 用户ID
        String orderSn = msg.getOrderToken(); // 订单号

        log.info("秒杀落库开始 -> User:{}, ProductId:{}", userId, productId);

        // 1. 根据商品ID查询秒杀活动
        SeckillStock skuInfo = seckillStockMapper.selectOne(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SeckillStock>()
                .eq(SeckillStock::getProductId, productId)
        );

        if (skuInfo == null) {
            log.error("秒杀活动不存在: productId={}", productId);
            throw new RuntimeException("秒杀活动不存在");
        }

        // 2. 扣减库存 (操作 hksc_order 库里的 seckill_stock 表)
        boolean updateResult = seckillStockMapper.update(null, new UpdateWrapper<SeckillStock>()
                .setSql("available_stock = available_stock - 1")
                .eq("id", skuInfo.getId()) // 使用秒杀活动ID
                .gt("available_stock", 0)
        ) > 0;

        if (!updateResult) {
            log.error("库存扣减失败(卖完了): skuId={}", skuInfo.getId());
            throw new RuntimeException("库存不足");
        }

        // 3. 插入主订单表
        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setUserId(userId);
        orderInfo.setOrderSn(orderSn);
        orderInfo.setTotalAmount(skuInfo.getSeckillPrice());
        orderInfo.setPayAmount(skuInfo.getSeckillPrice());
        orderInfo.setStatus(0); // 待付款
        orderInfo.setCreateTime(LocalDateTime.now());
        orderInfo.setNote("秒杀订单");

        orderMapper.insert(orderInfo);

        // 4. 插入订单明细表
        OrderItem orderItem = new OrderItem();
        orderItem.setOrderId(orderInfo.getId());
        orderItem.setSeckillId(skuInfo.getId()); // 秒杀活动ID
        orderItem.setProductId(productId); // 商品ID
        orderItem.setProductPrice(skuInfo.getSeckillPrice());
        orderItem.setBuyCount(1);
        orderItem.setProductName("秒杀商品");

        itemMapper.insert(orderItem);

        // 发送延迟消息用于超时取消（消息级别TTL：5分钟，秒杀订单过期时间短）
        rabbitTemplate.convertAndSend("", "order.delay.queue", orderInfo.getId().toString(), message -> {
            // ✅ 优化：设置消息级别TTL（5分钟 = 5 * 60 * 1000）
            message.getMessageProperties().setExpiration("300000");  // 5分钟
            return message;
        });

        log.info("秒杀订单完成: orderId={}, userId={}, productId={}", orderInfo.getId(), userId, productId);

        // 写入 Redis 成功标记
        // Key: seckill:success:{userId}:{productId}  Value: orderId
        // 设置过期时间 (比如 30 分钟)，没必要永久存，前端拿到结果就不查了
        String successKey = "seckill:success:" + userId + ":" + productId;
        stringRedisTemplate.opsForValue().set(successKey, orderInfo.getId().toString(), 30, TimeUnit.MINUTES);

    }

    @Override
    public Result<String> getSeckillOrderStatus(Long userId, Long skuId) {

        // 1. 【第一层】直接查 Redis 成功标记 (你的优化)
        // 这是最快路径！如果已经处理完了，直接返回，数据库一次都不用查。
        String successKey = "seckill:success:" + userId + ":" + skuId;
        String orderId = stringRedisTemplate.opsForValue().get(successKey);
        if (orderId != null) {
            return Result.success(orderId); // 恭喜，抢到了
        }

        // --- 如果上面没查到，说明：要么还在排队，要么失败了 ---

        // 2. 【第二层】查 Redis 库存
        String stockKey = "seckill:stock:" + skuId;
        String stockStr = stringRedisTemplate.opsForValue().get(stockKey);
        int stock;

        // 如果 Redis 读不到，尝试从数据库读取
        if (stockStr == null) {
            log.warn("Redis 库存为空，尝试从数据库读取: skuId={}", skuId);
            SeckillStock seckillStock = seckillStockMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SeckillStock>()
                    .eq(SeckillStock::getProductId, skuId)
            );

            if (seckillStock == null) {
                return Result.error("活动不存在");
            }

            stock = seckillStock.getAvailableStock();
            log.info("从数据库读取到库存: skuId={}, stock={}", skuId, stock);
        } else {
            stock = Integer.parseInt(stockStr);
        }

        // 3. 【第三层】状态裁决
        if (stock > 0) {
            // 库存还有，说明 MQ 肯定还在处理别人的或者你的
            return Result.build(null, 202, "排队中...");
        } else {
            // 库存 <= 0，此时进入“真空期”判断
            // 查防重名单，看我是否在“入围名单”里
            String userSetKey = "seckill:users:" + skuId;
            Boolean isMember = stringRedisTemplate.opsForSet().isMember(userSetKey, String.valueOf(userId));

            if (Boolean.TRUE.equals(isMember)) {
                // 在名单里，说明我是那个耗尽库存的人，正在等 Consumer 写 seckill:success
                return Result.build(null, 202, "排队中...");
            } else {
                // 不在名单里，说明真没抢到
                return Result.build(null, 201, "抢购失败");
            }
        }
    }

    // ================== 3. 查询订单详情  ==================
    @Override
    public Result<com.hksc.order.vo.OrderDetailVO> getOrderDetail(Long orderId, Long userId) {
        // 1. 查询订单主表
        OrderInfo orderInfo = orderMapper.selectById(orderId);
        if (orderInfo == null) {
            return Result.error("订单不存在");
        }

        // 2. 权限校验：只能查询自己的订单
        if (!orderInfo.getUserId().equals(userId)) {
            return Result.error("无权查看此订单");
        }

        // 3. 查询订单明细
        List<OrderItem> items = itemMapper.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<OrderItem>()
                .eq(OrderItem::getOrderId, orderId)
        );

        // 4. 转换为VO
        com.hksc.order.vo.OrderDetailVO orderVO = com.hksc.order.converter.OrderConverter.toDetailVO(orderInfo);
        orderVO.setItems(com.hksc.order.converter.OrderConverter.itemToVOList(items));

        return Result.success(orderVO);
    }

    // ================== 4. 查询订单列表  ==================
    @Override
    public Result<List<com.hksc.order.vo.OrderVO>> getOrderList(Long userId) {
        // 1. 查询用户所有订单
        List<OrderInfo> orders = orderMapper.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<OrderInfo>()
                .eq(OrderInfo::getUserId, userId)
                .orderByDesc(OrderInfo::getCreateTime)
        );

        if (orders.isEmpty()) {
            return Result.success(new java.util.ArrayList<>());
        }

        // 2. 转换为VO
        List<com.hksc.order.vo.OrderVO> resultList = new java.util.ArrayList<>();
        for (OrderInfo order : orders) {
            com.hksc.order.vo.OrderVO orderVO = com.hksc.order.converter.OrderConverter.toVO(order);

            // 查询订单明细（获取第一个商品信息）
            List<OrderItem> items = itemMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<OrderItem>()
                    .eq(OrderItem::getOrderId, order.getId())
            );

            if (!items.isEmpty()) {
                OrderItem firstItem = items.get(0);
                orderVO.setFirstProductName(firstItem.getProductName());
                orderVO.setFirstProductImage(firstItem.getProductImage());
                orderVO.setItemCount(items.size());
            }

            resultList.add(orderVO);
        }

        return Result.success(resultList);
    }

    // 辅助方法：订单状态转文字
    private String getOrderStatusText(Integer status) {
        if (status == null) return "未知";
        switch (status) {
            case 0: return "UNPAID";    // 待支付
            case 1: return "PAID";      // 已支付
            case 2: return "SHIPPED";   // 已发货
            case 3: return "COMPLETED"; // 已完成
            case 4: return "CANCELLED"; // 已取消
            default: return "未知";
        }
    }

    // ================== 5. 支付订单（模拟）  ==================
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<String> payOrder(Long orderId, Long userId) {
        log.info("开始支付订单: orderId={}, userId={}", orderId, userId);

        // 1. 查询订单
        OrderInfo order = orderMapper.selectById(orderId);
        if (order == null) {
            return Result.error("订单不存在");
        }

        // 2. 权限校验
        if (!order.getUserId().equals(userId)) {
            return Result.error("无权操作此订单");
        }

        // 3. 状态校验（只能支付待付款订单）
        if (order.getStatus() != 0) {
            return Result.error("订单状态异常，当前状态：" + getOrderStatusText(order.getStatus()));
        }

        // 4. 模拟支付成功，更新订单状态
        order.setStatus(1); // 1=已支付
        order.setPayTime(LocalDateTime.now());
        orderMapper.updateById(order);

        // 5. 发送MQ消息通知（可选，用于触发发货等后续流程）
        rabbitTemplate.convertAndSend("order.paid.queue", orderId.toString());

        log.info("订单支付成功: orderId={}", orderId);
        return Result.success("支付成功");
    }
}