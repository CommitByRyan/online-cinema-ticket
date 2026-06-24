package com.group7.olineordermeal.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.group7.olineordermeal.dto.DishSearchDTO;
import com.group7.olineordermeal.entity.Dish;
import com.group7.olineordermeal.mapper.DishMapper;
import com.group7.olineordermeal.service.DishService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class DishServiceImpl implements DishService {

    @Autowired
    private DishMapper dishMapper;

    @Override
    public PageInfo<Dish> searchDishPage(DishSearchDTO search, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<Dish> list = dishMapper.searchWithMerchant(search);
        return new PageInfo<>(list);
    }

    @Override
    public Dish getDishDetail(Long dishId) {
        return dishMapper.selectById(dishId);
    }

    @Override
    public List<Dish> getDishesByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) return List.of();
        return dishMapper.selectByIds(ids);
    }
}