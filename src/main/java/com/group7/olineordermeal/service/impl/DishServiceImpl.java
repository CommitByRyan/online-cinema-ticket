package com.group7.olineordermeal.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.group7.olineordermeal.dto.DishSearchDTO;
import com.group7.olineordermeal.entity.Dish;
import com.group7.olineordermeal.mapper.DishMapper;
import com.group7.olineordermeal.service.DishService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 菜品业务实现类
 */
@Service
public class DishServiceImpl implements DishService {

    // 菜品上架状态常量
    private static final String DISH_STATUS_ONLINE = "ONLINE";
    // 默认页码、页大小兜底
    private static final int DEFAULT_PAGE_NUM = 1;
    private static final int DEFAULT_PAGE_SIZE = 10;

    @Autowired
    private DishMapper dishMapper;

    /**
     * 菜品分页搜索（带商家信息）
     * @param search 搜索条件
     * @param pageNum 页码
     * @param pageSize 每页条数
     * @return 分页结果
     */
    @Override
    public PageInfo<Dish> searchDishPage(DishSearchDTO search, int pageNum, int pageSize) {
        // 分页参数容错
        pageNum = pageNum <= 0 ? DEFAULT_PAGE_NUM : pageNum;
        pageSize = pageSize <= 0 ? DEFAULT_PAGE_SIZE : pageSize;

        // 开启分页 + 默认按id倒序，保证分页数据稳定
        PageHelper.startPage(pageNum, pageSize);
        List<Dish> list = dishMapper.searchWithMerchant(search);
        return new PageInfo<>(list);
    }

    /**
     * 获取菜品详情（只返回上架菜品）
     * @param dishId 菜品id
     * @return 菜品实体
     */
    @Override
    public Dish getDishDetail(Long dishId) {
        if (dishId == null) {
            return null;
        }
        Dish dish = dishMapper.selectById(dishId);
        // 过滤已下架菜品
        if (dish != null && !DISH_STATUS_ONLINE.equals(dish.getStatus())) {
            return null;
        }
        return dish;
    }

    /**
     * 根据id批量查询菜品（自动过滤下架菜品）
     * @param ids 菜品id集合
     * @return 上架菜品列表
     */
    @Override
    public List<Dish> getDishesByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return new ArrayList<>();
        }
        List<Dish> allDishes = dishMapper.selectByIds(ids);
        // 过滤下架菜品
        List<Dish> onlineDishes = new ArrayList<>();
        for (Dish dish : allDishes) {
            if (dish != null && DISH_STATUS_ONLINE.equals(dish.getStatus())) {
                onlineDishes.add(dish);
            }
        }
        return onlineDishes;
    }
}