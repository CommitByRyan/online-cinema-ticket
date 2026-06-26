package com.group7.olineordermeal.mapper;

import com.group7.olineordermeal.entity.Dish;
import com.group7.olineordermeal.dto. DishSearchDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface DishMapper {

    /**
     * 根据ID查询菜品
     */
    Dish selectById(@Param("id") Long id);

    /**
     * 多条件分页查询菜品（联表查询商家名称）
     * @param search 搜索条件
     * @return 菜品列表，每个Dish对象会额外包含 merchantName 字段（可以用扩展对象或直接用Dish加一个临时属性）
     */
    List<Dish> searchWithMerchant(@Param("search") DishSearchDTO search);

    /**
     * 批量查询菜品（用于购物车展示）
     */
    List<Dish> selectByIds(@Param("ids") List<Long> ids);


    // 在 DishMapper.java 中添加
    List<Dish> selectByMerchantId(@Param("merchantId") Long merchantId);

    // 在 DishMapper.java 中添加
    int insert(Dish dish);
    int updateById(Dish dish);
    int deleteById(@Param("id") Long id);
}