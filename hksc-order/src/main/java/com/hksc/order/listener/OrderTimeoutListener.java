package com.hksc.order.listener;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hksc.order.entity.OrderInfo;
import com.hksc.order.entity.OrderItem;
import com.hksc.order.feign.ProductClient;
import com.hksc.order.mapper.OrderInfoMapper;
import com.hksc.order.mapper.OrderItemMapper;
import com.hksc.order.mapper.SeckillStockMapper;
import jakarta.annotation.Resource;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class OrderTimeoutListener {

    @Resource
    private OrderInfoMapper orderMapper;

    @Resource
    private OrderItemMapper orderItemMapper;

    @Resource
    private ProductClient productClient;

    //redis对应库存在超时后也需要回滚
    @Resource
    private SeckillStockMapper seckillStockMapper;
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 监听死信队列，处理超时未支付的订单
     */
    @RabbitListener(queues = "order.dlx.queue")
    @Transactional(rollbackFor = Exception.class) // 开启本地事务
    public void processTimeoutOrder(String orderIdStr) {
        // 1. 防御性判断
        if (orderIdStr == null || orderIdStr.isEmpty()) {
            return;
        }

        Long orderId = Long.valueOf(orderIdStr);
        System.out.println("【订单超时】收到过期订单消息，订单ID: " + orderId);

        // 2. 查询订单当前状态
        OrderInfo order = orderMapper.selectById(orderId);

        // 3. 只有状态为 0 (待付款) 的才需要取消
        // 如果用户已经付过钱了(status=1)，或者已经取消了(status=4)，就直接忽略
        if (order != null && order.getStatus() == 0) {

            // --- 3.1 修改订单状态为 4 (已取消) ---
            // 使用新建对象更新，只更新 status 字段，SQL 更高效
            OrderInfo updateInfo = new OrderInfo();
            updateInfo.setId(orderId); // Where 条件
            updateInfo.setStatus(4);   // Set 内容
            orderMapper.updateById(updateInfo);

            System.out.println("【订单超时】订单 " + orderId + " 状态已修改为 [已取消]");

            // --- 3.2 准备回滚库存 ---
            // 先去 order_item 表查一下这个订单买了哪些商品
            List<OrderItem> items = orderItemMapper.selectList(
                    new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, orderId)
            );

            // --- 3.3 遍历明细，远程调用 Product 服务把库存加回去 ---
            if (items != null && !items.isEmpty()) {
                for (OrderItem item : items) {
                    try {

                            // 检查 seckillId 是否存在
                        // 如果 seckillId 不为空，说明这是秒杀单；如果为空，说明是普通订单
                        if (item.getSeckillId() != null) {

                            // ================== 秒杀回滚逻辑 (新增) ==================
                            Long skuId = item.getSeckillId();
                            Long userId = order.getUserId(); // 从主订单拿用户ID

                            // 1. 回滚 MySQL 数据库库存 (seckill_sku 表)
                            // SQL: UPDATE seckill_sku SET seckill_stock = seckill_stock + 1 WHERE id = ?
                            seckillStockMapper.update(null, new com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper<com.hksc.order.entity.SeckillStock>()
                                    .setSql("seckill_stock = seckill_stock + 1")
                                    .eq("id", skuId)
                            );

                            // 2. 回滚 Redis 库存
                            // Key: seckill:stock:{skuId}
                            stringRedisTemplate.opsForValue().increment("seckill:stock:" + skuId);

                            // 3. 回滚 Redis 限购名单 (让用户能再次抢购)
                            // Key: seckill:users:{skuId}
                            stringRedisTemplate.opsForSet().remove("seckill:users:" + skuId, String.valueOf(userId));

                            // 4.删除 Redis 成功标记
                            String successKey = "seckill:success:" + userId + ":" + skuId;
                            stringRedisTemplate.delete(successKey);

                            System.out.println("【秒杀回滚】商品 " + skuId + " 全套回滚成功(DB+Redis库存+限购+成功标记)，用户 " + userId);

                        } else {
                            // ================== 普通订单回滚逻辑 ==================
                            // 远程 Feign 调用
                            productClient.restoreStock(item.getProductId(), item.getBuyCount());
                            System.out.println("【库存回滚】商品 " + item.getProductId() + " 库存已归还: " + item.getBuyCount());
                        }
                    } catch (Exception e) {
                        // 捕获异常，防止因为网络抖动导致整个事务回滚（订单取消失败）
                        // 在实际生产中，这里应该写入一张"异常日志表"，后续由定时任务重试
                        System.err.println("【严重警告】调用商品服务归还库存失败！商品ID: " + item.getProductId());
                        e.printStackTrace();
                    }
                }
            }

        } else {
            System.out.println("【订单超时】订单 " + orderId + " 当前状态不是待付款，无需处理。");
        }


    }
}