package com.hksc.order.feign;

import com.hksc.common.result.Result;
import com.hksc.order.dto.ProductDTO;
import com.hksc.order.feign.fallback.ProductClientFallback;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

// name = 服务名 (nacos里显示的名字)

@FeignClient(name = "hksc-product", fallback = ProductClientFallback.class)
public interface ProductClient {

    // 扣减库存
    @PostMapping("/product/deduct")
    Result<Boolean> deductStock(@RequestParam("productId") Long productId, @RequestParam("count") Integer count);

    // 查询商品DTO（用于Feign调用）
    @GetMapping("/product/dto/{id}")
    Result<ProductDTO> getProduct(@PathVariable("id") Long id);

    // 恢复库存
    @PostMapping("/product/restore")
    Result<Boolean> restoreStock(@RequestParam("productId") Long productId, @RequestParam("count") Integer count);
}