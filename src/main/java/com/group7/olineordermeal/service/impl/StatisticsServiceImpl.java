package com.group7.olineordermeal.service.impl;

import com.group7.olineordermeal.mapper.StatisticsMapper;
import com.group7.olineordermeal.service.StatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 统计报表的具体实现
 */
@Service
public class StatisticsServiceImpl implements StatisticsService {

    @Autowired
    private StatisticsMapper statisticsMapper;

    /** 已完成订单总数 */
    @Override
    public long getTotalOrderCount() {
        return statisticsMapper.countCompletedOrders();
    }

    /** 已完成订单总交易额 */
    @Override
    public BigDecimal getTotalRevenue() {
        return statisticsMapper.sumCompletedOrderAmount();
    }

    /** 近 7 天活跃用户数 */
    @Override
    public long getActiveUserCount() {
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        return statisticsMapper.countActiveUsersSince(sevenDaysAgo);
    }

    /** 近 7 天活跃商家数 */
    @Override
    public long getActiveMerchantCount() {
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        return statisticsMapper.countActiveMerchantsSince(sevenDaysAgo);
    }
}
