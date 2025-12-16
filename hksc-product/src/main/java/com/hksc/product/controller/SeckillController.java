package com.hksc.product.controller;

import com.hksc.common.result.Result;
import com.hksc.product.service.SeckillService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/product/seckill")
public class SeckillController {

    @Resource
    private SeckillService seckillService; // 改这里：使用接口类型，不要用 SeckillServiceImpl

    @PostMapping("/{skuId}")
    public Result<String> startSeckill(@PathVariable Long skuId,
                                       @RequestHeader("X-User-Id") Long userId) {
        return seckillService.seckill(skuId, userId);
    }
}