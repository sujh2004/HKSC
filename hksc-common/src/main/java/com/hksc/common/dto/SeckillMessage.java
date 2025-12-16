package com.hksc.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SeckillMessage implements Serializable {
    private Long userId;
    private Long skuId;
    private String orderToken; // 用于前端轮询结果的唯一凭证
}