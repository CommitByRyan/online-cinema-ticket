package com.group7.olineordermeal.service;



import java.math.BigDecimal;
import java.util.Map;

public interface StatisticsService {
    // 总订单数（已完成）
    long getTotalOrderCount();
    // 总交易额（已完成订单的总金额）
    BigDecimal getTotalRevenue();
    // 活跃用户数（近7天有已完成订单的用户）
    long getActiveUserCount();
    // 活跃商家数（近7天有已完成订单的商家）
    long getActiveMerchantCount();
}