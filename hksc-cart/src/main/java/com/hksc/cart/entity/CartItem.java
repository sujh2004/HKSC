package com.hksc.cart.entity;

import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class CartItem implements Serializable {
    private Long productId;    // 商品ID
    private String title;      // 标题
    private String image;      // 图片
    private BigDecimal price;  // 加入购物车时的价格
    private Integer count;     // 购买数量

    private Boolean checked = true;
}
