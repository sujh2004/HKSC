package com.hksc.cart.controller;

import com.hksc.cart.entity.CartItem;
import com.hksc.cart.service.CartService;
import com.hksc.common.result.Result;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/cart")
public class CartController {

    // 按照你的要求，使用 @Resource
    @Resource
    private CartService cartService;

    /**
     * 添加购物车
     * userId 暂时通过 Header 传递 (等后面有了网关 Filter 统一解析 Token 后会自动传)
     */
    @PostMapping("/add")
    public Result<Boolean> add(@RequestHeader(value = "X-User-Id", defaultValue = "1") Long userId,
                               @RequestParam Long productId,
                               @RequestParam Integer count) {
        cartService.addToCart(userId, productId, count);
        return Result.success(true);
    }

    /**
     * 购物车列表
     */
    @GetMapping("/list")
    public Result<List<CartItem>> list(@RequestHeader(value = "X-User-Id", defaultValue = "1") Long userId) {
        return Result.success(cartService.list(userId));
    }

    /**
     * 修改数量
     */
    @PostMapping("/update")
    public Result<Boolean> update(@RequestHeader(value = "X-User-Id", defaultValue = "1") Long userId,
                                  @RequestParam Long productId,
                                  @RequestParam Integer count) {
        cartService.updateCount(userId, productId, count);
        return Result.success(true);
    }

    /**
     * 删除
     */
    @PostMapping("/delete")
    public Result<Boolean> delete(@RequestHeader(value = "X-User-Id", defaultValue = "1") Long userId,
                                  @RequestParam Long productId) {
        cartService.delete(userId, productId);
        return Result.success(true);
    }
}