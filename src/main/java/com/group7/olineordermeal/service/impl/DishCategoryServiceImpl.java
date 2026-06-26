package com.group7.olineordermeal.service.impl;

import com.group7.olineordermeal.common.exception.BusinessException;
import com.group7.olineordermeal.dto.CategoryDTO;
import com.group7.olineordermeal.entity.DishCategory;
import com.group7.olineordermeal.mapper.DishCategoryMapper;
import com.group7.olineordermeal.service.DishCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class DishCategoryServiceImpl implements DishCategoryService {

    @Autowired
    private DishCategoryMapper categoryMapper;

    @Override
    public List<DishCategory> listByMerchant(Long merchantId) {
        return categoryMapper.selectByMerchantId(merchantId);
    }

    @Override
    public void addCategory(Long merchantId, CategoryDTO dto) {
        DishCategory category = new DishCategory();
        category.setMerchantId(merchantId);
        category.setCategoryName(dto.getCategoryName());
        category.setSortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : 0);
        categoryMapper.insert(category);
    }

    @Override
    public void updateCategory(Long id, CategoryDTO dto) {
        DishCategory category = categoryMapper.selectById(id);
        if (category == null) {
            throw new BusinessException("分类不存在");
        }
        category.setCategoryName(dto.getCategoryName());
        category.setSortOrder(dto.getSortOrder());
        categoryMapper.updateById(category);
    }

    @Override
    public void deleteCategory(Long id, Long merchantId) {
        DishCategory category = categoryMapper.selectById(id);
        if (category == null || !category.getMerchantId().equals(merchantId)) {
            throw new BusinessException("分类不存在或无权限");
        }
        // TODO: 检查该分类下是否有菜品，若有则应提示先转移或删除菜品
        categoryMapper.deleteById(id);
    }
}