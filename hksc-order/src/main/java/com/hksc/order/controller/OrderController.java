package com.hksc.order.controller;

import cn.hutool.core.lang.UUID;
import com.alibaba.csp.sentinel.EntryType;
import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.hksc.common.result.Result;
import com.hksc.common.utils.JwtUtils;
import com.hksc.order.dto.OrderCreateDTO;
import com.hksc.order.feign.CartClient;
import com.hksc.order.service.OrderService;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.slots.block.BlockException;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;


@RestController
@RequestMapping("/order")
public class OrderController {

    @Resource
    private OrderService orderService;

    @Autowired
    CartClient cartClient;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    /**
     * 创建订单接口 (集成 Sentinel 限流 + Redis 幂等性去重)
     * POST /api/order/create
     * Header:
     *   1. token (登录身份)
     *   2. Order-Token (防重令牌)
     */
    @PostMapping("/create")
    public Result<String> createOrder(@RequestHeader("token") String loginToken,
                                      @RequestHeader(value = "Order-Token", required = false) String orderToken,
                                      @RequestBody OrderCreateDTO dto) {

        Entry entry = null;
        try {
            // 1. 手动开启 Sentinel 限流保护
            entry = SphU.entry("createOrder", EntryType.IN, 1, loginToken);

            // ================== 👇👇👇 新增：幂等性检查 (防手抖) 👇👇👇 ==================
            if (!StringUtils.hasText(orderToken)) {
                return Result.error(400, "非法请求：缺少 Order-Token");
            }

            String tokenKey = "order:token:" + orderToken;
            // 原子删除：只有第一次请求能删成功并返回 true
            Boolean deleteSuccess = redisTemplate.delete(tokenKey);

            if (!Boolean.TRUE.equals(deleteSuccess)) {
                // 如果删除失败，说明这个 Token 已经被用过了（重复提交）
                return Result.error(409, "请勿重复提交订单！");
            }
            // ================== 👆👆👆 检查结束 👆👆👆 ==================


            // 2. 身份校验 (解析用户ID)
            Long userId = JwtUtils.getUserId(loginToken);
            if (userId == null) {
                return Result.error(401, "登录已过期或Token无效");
            }

            // 3. 执行下单业务
            return orderService.createOrder(userId, dto);

        } catch (BlockException ex) {
            // 捕获限流异常
            System.err.println("触发热点限流，Token: " + loginToken);
            return Result.error(429, "当前排队人数过多，请稍后再试！");
        } finally {
            if (entry != null) {
                entry.exit(1, loginToken);
            }
        }
    }





    // 简单的测试接口，验证服务是否活着
    @GetMapping("/test")
    public Result<String> test() {
        return Result.success("Order Service is working!");
    }

    /**
     * 查询秒杀结果 (前端轮询调用)
     * GET /api/order/seckill/status/{skuId}
     * Header: X-User-Id (可选，如果没有则从token解析)
     */
    @GetMapping("/seckill/status/{skuId}")
    public Result<String> getSeckillStatus(@PathVariable Long skuId,
                                           @RequestHeader(value = "X-User-Id", required = false) Long userId,
                                           @RequestHeader(value = "token", required = false) String token) {
        // 优先使用网关传递的userId，如果没有则从token解析
        if (userId == null) {
            if (token != null && !token.isEmpty()) {
                userId = JwtUtils.getUserId(token);
            }
        }

        if (userId == null) {
            return Result.error("用户未登录");
        }
        return orderService.getSeckillOrderStatus(userId, skuId);
    }

    /**
     * 获取防重令牌 (进入订单确认页时调用)
     */
    @GetMapping("/token")
    public Result<String> getOrderToken() {
        // 1. 生成随机 Token (去掉横杠)
        String token = UUID.randomUUID().toString().replace("-", "");

        // 2. 存入 Redis (Key="order:token:xxx", Value="1", 过期时间30分钟)
        String key = "order:token:" + token;
        redisTemplate.opsForValue().set(key, "1", 30, TimeUnit.MINUTES);

        return Result.success(token);
    }

    /**
     * 查询订单详情
     * GET /api/order/detail/{id}
     * Header: X-User-Id (可选，如果没有则从token解析)
     */
    @GetMapping("/detail/{id}")
    public Result<com.hksc.order.vo.OrderDetailVO> getOrderDetail(@PathVariable Long id,
                                                                  @RequestHeader(value = "X-User-Id", required = false) Long userId,
                                                                  @RequestHeader(value = "token", required = false) String token) {
        // 优先使用网关传递的userId，如果没有则从token解析
        if (userId == null) {
            if (token != null && !token.isEmpty()) {
                userId = JwtUtils.getUserId(token);
            }
        }

        if (userId == null) {
            return Result.error("用户未登录");
        }
        return orderService.getOrderDetail(id, userId);
    }

    /**
     * 查询用户订单列表
     * GET /api/order/list
     * Header: X-User-Id (可选，如果没有则从token解析)
     */
    @GetMapping("/list")
    public Result<List<com.hksc.order.vo.OrderVO>> getOrderList(@RequestHeader(value = "X-User-Id", required = false) Long userId,
                                                                 @RequestHeader(value = "token", required = false) String token) {
        // 优先使用网关传递的userId，如果没有则从token解析
        if (userId == null) {
            if (token != null && !token.isEmpty()) {
                userId = JwtUtils.getUserId(token);
            }
        }

        if (userId == null) {
            return Result.error("用户未登录");
        }
        return orderService.getOrderList(userId);
    }

    /**
     * 支付订单（模拟支付，无真实支付对接）
     * POST /api/order/pay/{orderId}
     * Header: X-User-Id (可选) 或 token
     */
    @PostMapping("/pay/{orderId}")
    public Result<String> payOrder(@PathVariable Long orderId,
                                   @RequestHeader(value = "X-User-Id", required = false) Long userId,
                                   @RequestHeader(value = "token", required = false) String token) {
        // 优先使用网关传递的userId，如果没有则从token解析
        if (userId == null) {
            if (token != null && !token.isEmpty()) {
                userId = JwtUtils.getUserId(token);
            }
        }

        if (userId == null) {
            return Result.error("用户未登录");
        }
        return orderService.payOrder(orderId, userId);
    }
}