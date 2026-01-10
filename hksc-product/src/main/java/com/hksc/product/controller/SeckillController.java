package com.hksc.product.controller;

import com.hksc.common.result.Result;
import com.hksc.common.utils.JwtUtils;
import com.hksc.product.service.SeckillService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/product/seckill")
public class SeckillController {

    @Resource
    private SeckillService seckillService;

    /**
     * 秒杀接口
     * POST /api/product/seckill/{skuId}
     * Header: X-User-Id (可选) 或 token
     */
    @PostMapping("/{skuId}")
    public Result<String> startSeckill(@PathVariable Long skuId,
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

        return seckillService.seckill(skuId, userId);
    }
}