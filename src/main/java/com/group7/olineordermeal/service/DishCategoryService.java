package com.group7.olineordermeal.service;

import com.group7.olineordermeal.dto.CategoryDTO;
import com.group7.olineordermeal.entity.DishCategory;
import java.util.List;

public interface DishCategoryService {
    List<DishCategory> listByMerchant(Long merchantId);
    void addCategory(Long merchantId, CategoryDTO dto);
    void updateCategory(Long id, CategoryDTO dto);
    void deleteCategory(Long id, Long merchantId);
}