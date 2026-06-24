package com.group7.olineordermeal.entity;

import lombok.Data;

/**
 * 购物车实体，对应表 tb_cart
 * 注意：购物车只存用户ID、菜品ID、数量，其他信息（价格、名称）需要关联查询
 */
@Data
public class Cart {
    private Long id;
    private Long userId;
    private Long dishId;
    private Integer quantity;
}