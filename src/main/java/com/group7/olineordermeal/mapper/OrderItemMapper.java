package com.group7.olineordermeal.mapper;

import com.group7.olineordermeal.entity.OrderItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface OrderItemMapper {
    int insert(OrderItem item);
    List<OrderItem> selectByOrderId(@Param("orderId") Long orderId);
}