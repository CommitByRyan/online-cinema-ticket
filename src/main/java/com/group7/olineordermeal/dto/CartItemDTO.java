package com.group7.olineordermeal.dto;

import lombok.Data;
import java.math.BigDecimal;

/**
 * 购物车展示项（包含菜品信息和数量）
 */
@Data
public class CartItemDTO {
    private Long dishId;
    private String dishName;
    private BigDecimal price;
    private String image;
    private Integer quantity;
    private BigDecimal subtotal;   // price * quantity
    private Long merchantId;
    private String merchantName;
}