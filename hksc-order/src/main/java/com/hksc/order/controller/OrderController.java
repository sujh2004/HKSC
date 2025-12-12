package com.hksc.order.controller;

import com.hksc.common.result.Result;
import com.hksc.common.utils.JwtUtils;
import com.hksc.order.dto.OrderCreateDTO;
import com.hksc.order.service.OrderService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/order")
public class OrderController {

    @Resource
    private OrderService orderService;

    /**
     * 创建订单接口
     * POST /api/order/create
     */
    @PostMapping("/create")
    public Result<String> createOrder(@RequestHeader("token") String token, @RequestBody OrderCreateDTO dto) {
        // 1. 从 Token 中解析 userId
        Long userId = JwtUtils.getUserId(token);

        if (userId == null) {
            return Result.error(401, "登录已过期或Token无效");
        }

        // 2. 调用业务逻辑
        return orderService.createOrder(userId, dto);
    }

    // 简单的测试接口，验证服务是否活着
    @GetMapping("/test")
    public Result<String> test() {
        return Result.success("Order Service is working!");
    }
}