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
    Order selectById(@Param("id") Long id);

    // ---------- 现有的查询 ----------
    List<Order> selectByUserId(@Param("userId") Long userId);
    List<Order> selectByUserIdAndStatus(@Param("userId") Long userId,
                                        @Param("status") String status);

    // ---------- 新增的商家相关查询 ----------
    List<Order> selectByMerchantId(@Param("merchantId") Long merchantId);

    List<Order> selectByMerchantIdAndStatus(@Param("merchantId") Long merchantId,
                                            @Param("status") String status);

    // ---------- 订单过期查询（已有） ----------
    List<Order> selectExpiredPendingOrders(@Param("now") LocalDateTime now);
}
