package com.group7.olineordermeal.service.impl;

import com.group7.olineordermeal.common.exception.BusinessException;
import com.group7.olineordermeal.dto.DishDTO;
import com.group7.olineordermeal.entity.Dish;
import com.group7.olineordermeal.mapper.DishMapper;
import com.group7.olineordermeal.service.MerchantDishService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MerchantDishServiceImpl implements MerchantDishService {

    @Autowired
    private DishMapper dishMapper;

    @Override
    public List<Dish> listByMerchant(Long merchantId) {
        return dishMapper.selectByMerchantId(merchantId);
    }

    @Override
    public Dish getDish(Long id) {
        return dishMapper.selectById(id);
    }

    @Override
    @Transactional
    public void addDish(Long merchantId, DishDTO dto) {
        Dish dish = new Dish();
        dish.setMerchantId(merchantId);
        dish.setCategoryId(dto.getCategoryId());
        dish.setDishName(dto.getDishName());
        dish.setPrice(dto.getPrice());
        dish.setOriginalPrice(dto.getOriginalPrice());
        dish.setImage(dto.getImage());
        dish.setDescription(dto.getDescription());
        dish.setStatus(dto.getStatus() != null ? dto.getStatus() : "ONLINE");
        dish.setSales(0);
        dishMapper.insert(dish);
    }

    @Override
    @Transactional
    public void updateDish(Long id, DishDTO dto) {
        Dish dish = dishMapper.selectById(id);
        if (dish == null) {
            throw new BusinessException("菜品不存在");
        }
        dish.setCategoryId(dto.getCategoryId());
        dish.setDishName(dto.getDishName());
        dish.setPrice(dto.getPrice());
        dish.setOriginalPrice(dto.getOriginalPrice());
        dish.setImage(dto.getImage());
        dish.setDescription(dto.getDescription());
        dish.setStatus(dto.getStatus());
        dishMapper.updateById(dish);
    }

    @Override
    @Transactional
    public void deleteDish(Long id, Long merchantId) {
        Dish dish = dishMapper.selectById(id);
        if (dish == null || !dish.getMerchantId().equals(merchantId)) {
            throw new BusinessException("菜品不存在或无权限删除");
        }
        dishMapper.deleteById(id);
    }

    // -------------------------------------------------
    //  新增：商家上下架（状态切换）
    // -------------------------------------------------
    @Override
    @Transactional
    public void updateStatus(Long id, String newStatus, Long merchantId) {
        Dish dish = dishMapper.selectById(id);
        if (dish == null) {
            throw new BusinessException("菜品不存在");
        }
        if (!dish.getMerchantId().equals(merchantId)) {
            throw new BusinessException("无权限操作别的商家菜品");
        }
        // 只允许在 ONLINE / OFFLINE 之间切换
        if (!"ONLINE".equals(newStatus) && !"OFFLINE".equals(newStatus)) {
            throw new BusinessException("状态只能为 ONLINE 或 OFFLINE");
        }
        dish.setStatus(newStatus);
        dishMapper.updateById(dish);
    }
}
