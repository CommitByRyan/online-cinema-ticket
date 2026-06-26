package com.group7.olineordermeal.service.impl;


import com.group7.olineordermeal.mapper.OrderMapper;
import com.group7.olineordermeal.mapper.UserMapper;
import com.group7.olineordermeal.mapper.MerchantMapper;
import com.group7.olineordermeal.service.StatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class StatisticsServiceImpl implements StatisticsService {

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private MerchantMapper merchantMapper;

    @Override
    public long getTotalOrderCount() {
        // 统计已完成订单数量
        return orderMapper.countCompletedOrders(); // 需要在 OrderMapper 中添加方法
    }

    @Override
    public BigDecimal getTotalRevenue() {
        return orderMapper.sumCompletedOrderAmount(); // 添加方法
    }

    @Override
    public long getActiveUserCount() {
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        return userMapper.countActiveUsersSince(sevenDaysAgo); // 添加方法
    }

    @Override
    public long getActiveMerchantCount() {
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        return merchantMapper.countActiveMerchantsSince(sevenDaysAgo); // 添加方法
    }
}