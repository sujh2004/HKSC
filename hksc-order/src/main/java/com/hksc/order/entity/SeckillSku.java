package com.hksc.order.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import java.util.Date;

@Data
@TableName("seckill_sku") // 指向数据库里的那张表
public class SeckillSku {
    private Long id;
    private Long productId;
    private BigDecimal seckillPrice;
    private Integer seckillStock;
    // 其他字段可省略，只要这几个关键的
}