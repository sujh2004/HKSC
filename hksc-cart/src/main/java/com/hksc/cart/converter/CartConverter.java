package com.hksc.cart.converter;

import com.hksc.cart.entity.CartItem;
import com.hksc.cart.vo.CartItemVO;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 购物车对象转换工具类
 * Entity <-> DTO <-> VO
 */
public class CartConverter {

    /**
     * CartItem -> CartItemVO
     */
    public static CartItemVO toVO(CartItem entity) {
        if (entity == null) {
            return null;
        }
        CartItemVO vo = new CartItemVO();
        vo.setProductId(entity.getProductId());
        vo.setTitle(entity.getTitle());
        vo.setPrice(entity.getPrice());
        vo.setImage(entity.getImage());
        vo.setCount(entity.getCount());
        vo.setStock(entity.getStock());
        vo.setChecked(entity.getChecked());

        // 计算小计
        if (entity.getPrice() != null && entity.getCount() != null) {
            vo.setSubtotal(entity.getPrice().multiply(BigDecimal.valueOf(entity.getCount())));
        }

        return vo;
    }

    /**
     * CartItem List -> CartItemVO List
     */
    public static List<CartItemVO> toVOList(List<CartItem> entities) {
        if (entities == null) {
            return null;
        }
        List<CartItemVO> voList = new ArrayList<>();
        for (CartItem entity : entities) {
            voList.add(toVO(entity));
        }
        return voList;
    }
}
