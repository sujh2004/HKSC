package com.hksc.cart.feign;

import com.hksc.cart.entity.Product;
import com.hksc.common.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * 商品服务的“备胎”
 */
@Slf4j
@Component
public class ProductClientFallback implements ProductClient {

    // ⚠️ 注意：这里加了 int sleep 参数，为了配合接口保持一致
    @Override
    public Result<Product> getProductDetail(Long id) {
        log.error("⚠️ 商品服务异常或超时，触发熔断降级！商品ID: {}", id);

        // 1. 造一个“兜底”数据
        Product defaultProduct = new Product();
        defaultProduct.setId(id);
        defaultProduct.setTitle("⛔ [降级数据] 商品详情暂时不可用");
        defaultProduct.setPrice(BigDecimal.ZERO);
        defaultProduct.setImage("default.png");

        // 2. 返回假装成功的 Result
        return Result.success(defaultProduct);
    }

}