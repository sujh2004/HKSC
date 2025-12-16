package com.hksc.product.controller;

import com.hksc.common.result.Result;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/product/stock")
public class StockController {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 【管理员工具】秒杀预热：把数据库的库存同步到 Redis
     * 用法：POST http://localhost:8083/api/product/stock/warmup?skuId=1&count=10
     */
    @PostMapping("/warmup")
    public Result<String> warmupStock(@RequestParam Long skuId, @RequestParam Integer count) {
        // 1. 设置库存 (Key 必须和 Lua 脚本里拼接的一致)
        String stockKey = "seckill:stock:" + skuId;
        stringRedisTemplate.opsForValue().set(stockKey, count.toString());

        // 2. 为了方便测试，把“已购买用户名单”也清空一下
        String userKey = "seckill:users:" + skuId;
        stringRedisTemplate.delete(userKey);

        return Result.success("预热成功！Redis Key: " + stockKey + ", 库存: " + count);
    }
}