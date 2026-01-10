package com.hksc.order.feign.fallback;

import com.hksc.common.result.Result;
import com.hksc.order.dto.ProductDTO;
import com.hksc.order.feign.ProductClient;
import org.springframework.stereotype.Component;

@Component
public class ProductClientFallback implements ProductClient {
    @Override
    public Result<Boolean> deductStock(Long productId, Integer count) {
        return Result.error(503, "库存系统繁忙，下单失败 (熔断降级)");
    }

    @Override
    public Result<ProductDTO> getProduct(Long id) {
        return Result.error(503, "商品信息不可用");
    }

    @Override
    public Result<Boolean> restoreStock(Long productId, Integer count) {
        return Result.error(503, "库存回滚失败");
    }


}