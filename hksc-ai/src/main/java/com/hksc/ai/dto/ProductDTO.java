package com.hksc.ai.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class ProductDTO {
    private Long id;
    private String title;
    private BigDecimal price;
    private String image;
}