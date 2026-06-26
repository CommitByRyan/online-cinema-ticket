package com.group7.olineordermeal.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class DishDTO {
    private Long id;               // 编辑时才有id
    private Long categoryId;
    private String dishName;
    private BigDecimal price;
    private BigDecimal originalPrice;
    private String image;
    private String description;
    private String status;         // ONLINE / OFFLINE
}