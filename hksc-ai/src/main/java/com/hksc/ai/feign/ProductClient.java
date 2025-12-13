package com.hksc.ai.feign;

import com.hksc.ai.dto.ProductDTO;
import com.hksc.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

// 呼叫 hksc-product 服务
@FeignClient(name = "hksc-product")
public interface ProductClient {

    // 注意：路径要和 ProductController 真实路径一致 (不要加 /api 前缀)
    // 假设 ProductController 类上有 @RequestMapping("/product")
    @GetMapping("/product/detail/{id}")
    Result<ProductDTO> getProduct(@PathVariable("id") Long id);
}