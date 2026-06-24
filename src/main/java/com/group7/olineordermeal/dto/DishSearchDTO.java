package com.group7.olineordermeal.dto;

import lombok.Data;
import java.math.BigDecimal;

import lombok.Data;
import java.math.BigDecimal;

/**
 * 菜品搜索条件
 */
@Data
public class DishSearchDTO {
    private String dishName;      // 菜品名（模糊）
    private BigDecimal minPrice;  // 最低价
    private BigDecimal maxPrice;  // 最高价
    private String merchantName;  // 商家名（模糊）
    private String category;      // 分类（预留，若需要）
}

