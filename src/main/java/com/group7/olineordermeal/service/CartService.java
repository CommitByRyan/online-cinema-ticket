package com.group7.olineordermeal.service;

import com.group7.olineordermeal.dto.CartItemDTO;
import java.util.List;

public interface CartService {
    /**
     * 添加菜品到购物车（如果已存在则增加数量）
     */
    void addToCart(Long userId, Long dishId, Integer quantity);

    /**
     * 修改购物车中某个菜品的数量
     */
    void updateQuantity(Long userId, Long dishId, Integer quantity);

    /**
     * 删除购物车中的某个菜品
     */
    void removeFromCart(Long userId, Long dishId);

    /**
     * 清空购物车
     */
    void clearCart(Long userId);

    /**
     * 获取购物车列表（带菜品详情）
     */
    List<CartItemDTO> getCartItems(Long userId);

    /**
     * 获取购物车中商家的ID（用于强制单商家校验）
     */
    Long getCartMerchantId(Long userId);
}
