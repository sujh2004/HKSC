package com.hksc.cart.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONUtil;
import com.hksc.cart.entity.CartItem;
import com.hksc.cart.entity.Product;
import com.hksc.cart.feign.ProductClient;
import com.hksc.cart.service.CartService;
import com.hksc.common.result.Result;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CartServiceImpl implements CartService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private ProductClient productClient;

    private static final String CART_PREFIX = "cart:user:";

    @Override
    public void addToCart(Long userId, Long productId, Integer count) {
        String key = CART_PREFIX + userId;

        // 1. 先查 Redis 里有没有这个商品
        Object json = stringRedisTemplate.opsForHash().get(key, productId.toString());

        CartItem cartItem;
        if (json != null) {
            // 2. 如果有，只加数量
            cartItem = JSONUtil.toBean((String) json, CartItem.class);
            cartItem.setCount(cartItem.getCount() + count);
        } else {
            // 3. 如果没有，远程调用商品服务查询详情
            Result<Product> result = productClient.getProductDetail(productId);
            if (result.getData() == null) {
                throw new RuntimeException("商品不存在");
            }
            Product product = BeanUtil.copyProperties(result.getData(), Product.class); // 类型转换

            // 4. 封装成 CartItem
            cartItem = new CartItem();
            cartItem.setProductId(productId);
            cartItem.setTitle(product.getTitle());
            cartItem.setPrice(product.getPrice());
            cartItem.setImage(product.getImage());
            cartItem.setCount(count);
            cartItem.setChecked(true);
        }

        // 5. 写回 Redis
        stringRedisTemplate.opsForHash().put(key, productId.toString(), JSONUtil.toJsonStr(cartItem));
    }

    @Override
    public List<CartItem> list(Long userId) {
        String key = CART_PREFIX + userId;
        // 获取 Hash 中所有的值 (CartItem JSON)
        List<Object> values = stringRedisTemplate.opsForHash().values(key);

        return values.stream()
                .map(obj -> JSONUtil.toBean((String) obj, CartItem.class))
                .collect(Collectors.toList());
    }

    @Override
    public void updateCount(Long userId, Long productId, Integer count) {
        String key = CART_PREFIX + userId;
        Object json = stringRedisTemplate.opsForHash().get(key, productId.toString());
        if (json != null) {
            CartItem item = JSONUtil.toBean((String) json, CartItem.class);
            item.setCount(count);
            stringRedisTemplate.opsForHash().put(key, productId.toString(), JSONUtil.toJsonStr(item));
        }
    }

    @Override
    public void delete(Long userId, Long productId) {
        String key = CART_PREFIX + userId;
        stringRedisTemplate.opsForHash().delete(key, productId.toString());
    }
}