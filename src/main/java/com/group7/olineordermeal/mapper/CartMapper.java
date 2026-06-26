package com.group7.olineordermeal.mapper;

import com.group7.olineordermeal.entity.Cart;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;


@Mapper
public interface CartMapper {

    /**
     * 查询某用户的所有购物车记录
     */
    List<Cart> selectByUserId(@Param("userId") Long userId);

    /**
     * 查询某用户在某个菜品上的记录
     */
    Cart selectByUserIdAndDishId(@Param("userId") Long userId, @Param("dishId") Long dishId);

    /**
     * 插入购物车记录
     */
    int insert(Cart cart);

    /**
     * 更新数量
     */
    int updateQuantity(@Param("id") Long id, @Param("quantity") Integer quantity);

    /**
     * 删除某条购物车记录
     */
    int deleteById(@Param("id") Long id);

    /**
     * 清空某用户的所有购物车
     */
    int deleteByUserId(@Param("userId") Long userId);

    /**
     * 根据用户ID和菜品ID删除（用于数量减到0时）
     */
    int deleteByUserIdAndDishId(@Param("userId") Long userId, @Param("dishId") Long dishId);
}
