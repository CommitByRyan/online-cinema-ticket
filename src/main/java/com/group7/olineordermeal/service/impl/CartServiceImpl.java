package com.group7.olineordermeal.service.impl;


import com.group7.olineordermeal.common.exception.BusinessException;
import com.group7.olineordermeal.dto.CartItemDTO;
import com.group7.olineordermeal.entity.Cart;
import com.group7.olineordermeal.entity.Dish;
import com.group7.olineordermeal.mapper.CartMapper;
import com.group7.olineordermeal.mapper.DishMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private CartMapper cartMapper;

    @Autowired
    private DishMapper dishMapper;

    @Override
    @Transactional
    public void addToCart(Long userId, Long dishId, Integer quantity) {
        // 1. 查询要添加的菜品
        Dish dish = dishMapper.selectById(dishId);
        if (dish == null || !"ONLINE".equals(dish.getStatus())) {
            throw new BusinessException("菜品不存在或已下架");
        }
        // 2. 检查购物车是否已有其他商家的菜品（强制单商家）
        Long existingMerchantId = getCartMerchantId(userId);
        if (existingMerchantId != null && !existingMerchantId.equals(dish.getMerchantId())) {
            throw new BusinessException("购物车已有其他商家的菜品，请先清空购物车或完成下单");
        }
        // 3. 插入或更新购物车（Mapper 中使用了 ON DUPLICATE KEY UPDATE）
        Cart cart = new Cart();
        cart.setUserId(userId);
        cart.setDishId(dishId);
        cart.setQuantity(quantity);
        cartMapper.insert(cart);
    }

    @Override
    @Transactional
    public void updateQuantity(Long userId, Long dishId, Integer quantity) {
        if (quantity <= 0) {
            removeFromCart(userId, dishId);
            return;
        }
        Cart cart = cartMapper.selectByUserIdAndDishId(userId, dishId);
        if (cart == null) {
            throw new BusinessException("购物车中不存在该菜品");
        }
        cart.setQuantity(quantity);
        cartMapper.updateQuantity(cart.getId(), quantity);
    }

    @Override
    @Transactional
    public void removeFromCart(Long userId, Long dishId) {
        cartMapper.deleteByUserIdAndDishId(userId, dishId);
    }

    @Override
    @Transactional
    public void clearCart(Long userId) {
        cartMapper.deleteByUserId(userId);
    }

    @Override
    public List<CartItemDTO> getCartItems(Long userId) {
        List<Cart> carts = cartMapper.selectByUserId(userId);
        if (carts.isEmpty()) {
            return List.of();
        }
        // 批量查询菜品信息
        List<Long> dishIds = carts.stream().map(Cart::getDishId).collect(Collectors.toList());
        List<Dish> dishes = dishMapper.selectByIds(dishIds);
        Map<Long, Dish> dishMap = dishes.stream().collect(Collectors.toMap(Dish::getId, d -> d));

        List<CartItemDTO> result = new ArrayList<>();
        for (Cart cart : carts) {
            Dish dish = dishMap.get(cart.getDishId());
            if (dish == null) continue;
            CartItemDTO item = new CartItemDTO();
            item.setDishId(dish.getId());
            item.setDishName(dish.getDishName());
            item.setPrice(dish.getPrice());
            item.setImage(dish.getImage());
            item.setQuantity(cart.getQuantity());
            item.setSubtotal(dish.getPrice().multiply(BigDecimal.valueOf(cart.getQuantity())));
            item.setMerchantId(dish.getMerchantId());
            item.setMerchantName(dish.getMerchantName());
            result.add(item);
        }
        return result;
    }

    @Override
    public Long getCartMerchantId(Long userId) {
        List<Cart> carts = cartMapper.selectByUserId(userId);
        if (carts.isEmpty()) return null;
        // 取第一个菜品的商家ID（因为强制单商家，所有菜品属于同一商家）
        Long dishId = carts.get(0).getDishId();
        Dish dish = dishMapper.selectById(dishId);
        return dish != null ? dish.getMerchantId() : null;
    }
}
