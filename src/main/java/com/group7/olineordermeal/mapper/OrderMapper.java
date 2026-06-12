package com.group7.olineordermeal.mapper;

import com.group7.olineordermeal.entity.Order;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface OrderMapper {
    int insert(Order order);
    int updateById(Order order);
    Order selectById(Long id);
    List<Order> selectExpiredPendingOrders(@Param("now") LocalDateTime now);
    // 其他方法可以根据需要增加
}