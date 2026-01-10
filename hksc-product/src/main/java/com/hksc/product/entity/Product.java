package com.hksc.product.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("product")
public class Product implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String title;

    private BigDecimal price;

    private Long categoryId;

    private String brand;

    private String image;

    private String images;

    private String detail;

    private Integer status; // 1上架 0下架

    @TableField("is_seckill")
    private Integer isSeckill; // 是否为秒杀商品: 1是/0否

    private Integer sales; // 销量

    @TableLogic // 逻辑删除
    private Integer deleted; // 0未删除/1已删除

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    // ===== 非数据库字段，用于前端展示 =====
    @TableField(exist = false)
    private Integer stock; // 从product_stock表查询后填充
}
