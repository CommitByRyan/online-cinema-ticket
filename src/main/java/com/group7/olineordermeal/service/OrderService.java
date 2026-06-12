package com.group7.olineordermeal.service;

public interface OrderService {
    int cancelExpiredPendingOrders();  // 自动取消过期订单
}