package com.hksc.order.controller;

import com.alibaba.csp.sentinel.EntryType;
import com.hksc.common.result.Result;
import com.hksc.common.utils.JwtUtils;
import com.hksc.order.dto.OrderCreateDTO;
import com.hksc.order.service.OrderService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;
import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.slots.block.BlockException;


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
// @SentinelResource... 删掉这一行注解
    public Result<String> createOrder(@RequestHeader("token") String token, @RequestBody OrderCreateDTO dto) {

        Entry entry = null;
        try {
            // 1. 手动开启限流保护 (资源名: createOrder, 参数: token)
            // 这里的 createOrder 必须和控制台配的一样
            // EntryType.IN 代表这是入口流量
            entry = SphU.entry("createOrder", EntryType.IN, 1, token);

            // 2. 原有的业务逻辑 (解析用户ID -> 调用Service)
            Long userId = JwtUtils.getUserId(token);
            if (userId == null) {
                return Result.error(401, "登录已过期或Token无效");
            }
            return orderService.createOrder(userId, dto);

        } catch (BlockException ex) {
            // 3. 捕获限流异常 (相当于原来的 blockHandler)
            System.err.println("触发热点限流(手动埋点)，Token: " + token);
            return Result.error(429, "您下单太快了，请稍后再试！(手动限流生效)");
        } finally {
            // 4. 必须释放 entry
            if (entry != null) {
                entry.exit(1, token);
            }
        }
    }

    // 简单的测试接口，验证服务是否活着
    @GetMapping("/test")
    public Result<String> test() {
        return Result.success("Order Service is working!");
    }
}