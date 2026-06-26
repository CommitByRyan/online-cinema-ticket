package com.group7.olineordermeal.service;

import java.math.BigDecimal;

/**
 * 平台运营统计 Service
 * 供管理员页面使用
 */
public interface StatisticsService {

    /** 已完成订单总数 */
    long getTotalOrderCount();

    /** 已完成订单总交易额（已完成订单的 sum） */
    BigDecimal getTotalRevenue();

    /** 近 7 天活跃用户数（有已完成订单的用户） */
    long getActiveUserCount();

    /** 近 7 天活跃商家数（有已完成订单的商家） */
    long getActiveMerchantCount();
}
