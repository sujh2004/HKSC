package com.hksc.search.feign;

import com.hksc.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "hksc-product")
public interface ProductClient {
    // 调用商品列表接口 (我们需要改一下商品服务，增加一个不分页查所有的接口，或者这里直接分页查第一页)
    // 为了简单，我们复用 list 接口
    @GetMapping("/product/list")
    Result<Object> list(@RequestParam("page") Integer page, @RequestParam("limit") Integer limit);
}