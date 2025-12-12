package com.hksc.cart.feign;

import com.hksc.cart.entity.Product;
import com.hksc.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "hksc-product")
public interface ProductClient {
    @GetMapping("/product/detail/{id}")
    Result<Product> getProductDetail(@PathVariable("id") Long id);
}
