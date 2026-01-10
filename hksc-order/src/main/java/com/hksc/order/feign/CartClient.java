package com.hksc.order.feign;

import com.hksc.common.result.Result;
import com.hksc.order.feign.fallback.CartClientFallback;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// value = "hksc-cart" 是购物车服务在 Nacos 里的名字
// fallback = ... 指定刚才写的备胎类
@FeignClient(value = "hksc-cart", fallback = CartClientFallback.class)
public interface CartClient {
    /**
     * 远程调用：删除购物车中选中的商品
     */
    @PostMapping("/cart/delete/checked")
    Result<Boolean> deleteChecked(@RequestHeader("X-User-Id") Long userId, // 👈 加这个
                                  @RequestBody List<Long> skuIds);

    @GetMapping("/cart/clear")
    String clearCart();
}