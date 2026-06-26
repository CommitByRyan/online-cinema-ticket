package com.group7.olineordermeal.entity;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class OrderItem {
    private Long id;
    private Long orderId;
    private Long dishId;
    private String dishName;   // 快照
    private BigDecimal price;  // 快照
    private Integer quantity;
    private BigDecimal subtotal;
}