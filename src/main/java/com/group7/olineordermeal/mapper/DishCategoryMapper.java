package com.group7.olineordermeal.mapper;

import com.group7.olineordermeal.entity.DishCategory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface DishCategoryMapper {
    List<DishCategory> selectByMerchantId(@Param("merchantId") Long merchantId);
    DishCategory selectById(@Param("id") Long id);
    int insert(DishCategory category);
    int updateById(DishCategory category);
    int deleteById(@Param("id") Long id);
}