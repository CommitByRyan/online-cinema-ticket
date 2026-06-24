package com.group7.olineordermeal.service;

import com.group7.olineordermeal.entity.Dish;
import com.group7.olineordermeal.dto.DishSearchDTO;
import com.github.pagehelper.PageInfo;
import java.util.List;

public interface DishService {
    /**
     * 分页搜索菜品
     */
    PageInfo<Dish> searchDishPage(DishSearchDTO search, int pageNum, int pageSize);

    /**
     * 根据ID获取菜品详情
     */
    Dish getDishDetail(Long dishId);

    /**
     * 批量获取菜品（用于购物车）
     */
    List<Dish> getDishesByIds(List<Long> ids);
}
