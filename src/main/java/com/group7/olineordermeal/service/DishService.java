package com.group7.olineordermeal.service;

import com.github.pagehelper.PageInfo;
import com.group7.olineordermeal.dto.DishSearchDTO;
import com.group7.olineordermeal.entity.Dish;
import java.util.List;

public interface DishService {

    PageInfo<Dish> searchDishPage(DishSearchDTO search, int pageNum, int pageSize);

    Dish getDishDetail(Long dishId);

    List<Dish> getDishesByIds(List<Long> ids);
}