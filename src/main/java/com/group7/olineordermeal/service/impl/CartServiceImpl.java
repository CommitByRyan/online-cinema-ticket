package com.group7.olineordermeal.service.impl;

import com.group7.olineordermeal.common.exception.BusinessException;
import com.group7.olineordermeal.dto.CartItemDTO;
import com.group7.olineordermeal.entity.Cart;
import com.group7.olineordermeal.entity.Dish;
import com.group7.olineordermeal.mapper.CartMapper;
import com.group7.olineordermeal.mapper.DishMapper;
import com.group7.olineordermeal.service.CartService;
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

    private static final String DISH_STATUS_ONLINE = "ONLINE";
    private static final Integer MAX_BUY_NUM = 999;

    @Autowired
    private CartMapper cartMapper;

    @Autowired
    private DishMapper dishMapper;

    @Override
    @Transactional
    public void addToCart(Long userId, Long dishId, Integer quantity) {
        // 入参合法性校验
        if (userId == null || dishId == null) {
            throw new BusinessException("用户ID或菜品ID不能为空");
        }
        if (quantity == null || quantity < 1) {
            throw new BusinessException("购买数量必须大于0");
        }
        if (quantity > MAX_BUY_NUM) {
            throw new BusinessException("单次购买数量不能超过" + MAX_BUY_NUM);
        }

        // 判断菜品是否存在且上架
        Dish dish = dishMapper.selectById(dishId);
        if (dish == null || !DISH_STATUS_ONLINE.equals(dish.getStatus())) {
            throw new BusinessException("菜品不存在或已下架");
        }

        // 校验购物车只能存放同一商家商品
        Long existMerchantId = getCartMerchantId(userId);
        if (existMerchantId != null && !existMerchantId.equals(dish.getMerchantId())) {
            throw new BusinessException("购物车已有其他商家的菜品，请先清空购物车或完成下单");
        }

        // 查询当前购物车是否已有该菜品，有则累加数量，无则新增
        Cart existCart = cartMapper.selectByUserIdAndDishId(userId, dishId);
        if (existCart != null) {
            int newQty = existCart.getQuantity() + quantity;
            if (newQty > MAX_BUY_NUM) {
                throw new BusinessException("该菜品购物车总数量超出上限" + MAX_BUY_NUM);
            }
            cartMapper.updateQuantity(existCart.getId(), newQty);
            return;
        }

        Cart cart = new Cart();
        cart.setUserId(userId);
        cart.setDishId(dishId);
        cart.setQuantity(quantity);
        cartMapper.insert(cart);
    }

    @Override
    @Transactional
    public void updateQuantity(Long userId, Long dishId, Integer quantity) {
        if (userId == null || dishId == null) {
            throw new BusinessException("用户ID或菜品ID不能为空");
        }
        // 数量<=0直接删除
        if (quantity == null || quantity <= 0) {
            removeFromCart(userId, dishId);
            return;
        }
        if (quantity > MAX_BUY_NUM) {
            throw new BusinessException("修改数量不能超过" + MAX_BUY_NUM);
        }

        Cart cart = cartMapper.selectByUserIdAndDishId(userId, dishId);
        if (cart == null) {
            throw new BusinessException("购物车中不存在该菜品");
        }
        cartMapper.updateQuantity(cart.getId(), quantity);
    }

    @Override
    @Transactional
    public void removeFromCart(Long userId, Long dishId) {
        if (userId == null || dishId == null) {
            throw new BusinessException("用户ID或菜品ID不能为空");
        }
        cartMapper.deleteByUserIdAndDishId(userId, dishId);
    }

    @Override
    @Transactional
    public void clearCart(Long userId) {
        if (userId == null) {
            throw new BusinessException("用户ID不能为空");
        }
        cartMapper.deleteByUserId(userId);
    }

    @Override
    public List<CartItemDTO> getCartItems(Long userId) {
        if (userId == null) {
            return new ArrayList<>();
        }
        List<Cart> carts = cartMapper.selectByUserId(userId);
        if (carts.isEmpty()) {
            return new ArrayList<>();
        }

        // 批量查菜品，减少数据库IO
        List<Long> dishIdList = carts.stream()
                .map(Cart::getDishId)
                .collect(Collectors.toList());
        List<Dish> dishList = dishMapper.selectByIds(dishIdList);
        Map<Long, Dish> dishMap = dishList.stream()
                .collect(Collectors.toMap(Dish::getId, d -> d));

        List<CartItemDTO> result = new ArrayList<>();
        for (Cart cart : carts) {
            Dish dish = dishMap.get(cart.getDishId());
            // 过滤已删除、已下架菜品
            if (dish == null || !DISH_STATUS_ONLINE.equals(dish.getStatus())) {
                continue;
            }

            CartItemDTO item = new CartItemDTO();
            item.setDishId(dish.getId());
            item.setDishName(dish.getDishName());

            // 价格空值保护，防止空指针
            BigDecimal price = dish.getPrice() == null ? BigDecimal.ZERO : dish.getPrice();
            item.setPrice(price);
            item.setImage(dish.getImage());
            item.setQuantity(cart.getQuantity());
            item.setSubtotal(price.multiply(BigDecimal.valueOf(cart.getQuantity())));
            item.setMerchantId(dish.getMerchantId());
            item.setMerchantName(dish.getMerchantName());

            result.add(item);
        }
        return result;
    }

    @Override
    public Long getCartMerchantId(Long userId) {
        if (userId == null) {
            return null;
        }
        List<Cart> carts = cartMapper.selectByUserId(userId);
        if (carts.isEmpty()) {
            return null;
        }
        Long firstDishId = carts.get(0).getDishId();
        Dish dish = dishMapper.selectById(firstDishId);
        return dish == null ? null : dish.getMerchantId();
    }
}