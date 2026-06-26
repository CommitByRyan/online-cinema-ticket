package com.group7.olineordermeal.service;

import com.group7.olineordermeal.dto.CartItemDTO;
import java.util.List;

public interface CartService {

    void addToCart(Long userId, Long dishId, Integer quantity);

    void updateQuantity(Long userId, Long dishId, Integer quantity);

    void removeFromCart(Long userId, Long dishId);

    void clearCart(Long userId);

    List<CartItemDTO> getCartItems(Long userId);

    Long getCartMerchantId(Long userId);
}