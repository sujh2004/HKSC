package com.hksc.cart.service;

import com.hksc.cart.entity.CartItem;

import java.util.List;

public interface CartService {
    void addToCart(Long userId, Long productId, Integer count);
    List<CartItem> list(Long userId);
    void updateCount(Long userId, Long productId, Integer count);
    void delete(Long userId, Long productId);

    /**
     * 批量删除 (增加了 userId 参数)
     */
    void deleteBatch(Long userId, List<Long> productIds);
}
