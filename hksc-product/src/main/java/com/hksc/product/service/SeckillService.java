package com.hksc.product.service;

import com.hksc.common.result.Result;

public interface SeckillService {
    /**
     * 秒杀核心接口
     * @param skuId 商品ID
     * @param userId 用户ID
     * @return Result<String> 返回排队Token或错误信息
     */
    Result<String> seckill(Long skuId, Long userId);
}