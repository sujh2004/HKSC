package com.hksc.cart.controller;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.hksc.cart.dto.CartAddDTO;
import com.hksc.cart.dto.CartUpdateDTO;
import com.hksc.cart.entity.CartItem;
import com.hksc.cart.feign.ProductClient;
import com.hksc.cart.service.CartService;
import com.hksc.common.result.Result;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/cart")
public class CartController {

    // 按照你的要求，使用 @Resource
    @Resource
    private CartService cartService;

    @Autowired
    private ProductClient productClient;

    /**
     * 添加购物车
     * userId 暂时通过 Header 传递 (等后面有了网关 Filter 统一解析 Token 后会自动传)
     */
    @PostMapping("/add")
    public Result<Boolean> add(@RequestHeader(value = "X-User-Id", defaultValue = "1") Long userId,
                               @RequestBody CartAddDTO dto) {
        cartService.addToCart(userId, dto.getProductId(), dto.getCount());
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
                                  @RequestBody CartUpdateDTO dto) {
        cartService.updateCount(userId, dto.getProductId(), dto.getCount());
        return Result.success(true);
    }

    /**
     * 删除
     */
    @PostMapping("/delete")
    public Result<Boolean> delete(@RequestHeader(value = "X-User-Id", defaultValue = "1") Long userId,
                                  @RequestBody CartAddDTO dto) {
        cartService.delete(userId, dto.getProductId());
        return Result.success(true);
    }

    /**
     * 【新增】批量删除选中的商品 (供订单服务熔断调用)
     * 对应路径: /cart/delete/checked
     */
    @PostMapping("/delete/checked")
    public Result<Boolean> deleteChecked(@RequestHeader("X-User-Id") Long userId,
                                         @RequestBody List<Long> productIds) {
        // 这里的 userId 是网关传过来的，或者前端传的，必须要有
        cartService.deleteBatch(userId, productIds);
        return Result.success(true);
    }


}
