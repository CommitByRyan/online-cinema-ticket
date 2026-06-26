package com.group7.olineordermeal.service;

import com.group7.olineordermeal.dto.DishDTO;
import com.group7.olineordermeal.entity.Dish;
import java.util.List;

/**
 * 商家端菜品管理（只允许对自己商家的菜品操作）
 */
public interface MerchantDishService {
    List<Dish> listByMerchant(Long merchantId);
    Dish getDish(Long id);
    void addDish(Long merchantId, DishDTO dto);
    void updateDish(Long id, DishDTO dto);
    void deleteDish(Long id, Long merchantId);
    void updateStatus(Long id, String newStatus, Long merchantId); // ← 新增
}