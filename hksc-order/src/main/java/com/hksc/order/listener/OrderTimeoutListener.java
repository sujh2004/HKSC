package com.hksc.order.listener;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hksc.order.entity.OrderInfo;
import com.hksc.order.entity.OrderItem;
import com.hksc.order.feign.ProductClient;
import com.hksc.order.mapper.OrderInfoMapper;
import com.hksc.order.mapper.OrderItemMapper;
import jakarta.annotation.Resource;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
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
                        // 远程 Feign 调用
                        productClient.restoreStock(item.getProductId(), item.getQuantity());
                        System.out.println("【库存回滚】商品 " + item.getProductId() + " 库存已归还: " + item.getQuantity());
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