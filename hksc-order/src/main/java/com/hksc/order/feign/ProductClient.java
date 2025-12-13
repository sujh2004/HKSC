package com.hksc.order.feign;

import com.hksc.common.result.Result;
import com.hksc.order.dto.ProductDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

// name = 服务名 (nacos里显示的名字)
@FeignClient(name = "hksc-product")
public interface ProductClient {

    // 假设开发者B会提供这个接口：扣减库存
    @PostMapping("/product/deduct")
    Result<Boolean> deductStock(@RequestParam("productId") Long productId, @RequestParam("count") Integer count);

    // 查询商品价格
    @GetMapping("/product/detail/{id}")
    Result<ProductDTO> getProduct(@PathVariable("id") Long id);
}