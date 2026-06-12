package com.group7.olineordermeal.service.impl;

import com.group7.olineordermeal.common.enums.OrderStatusEnum;
import com.group7.olineordermeal.entity.Order;
import com.group7.olineordermeal.mapper.OrderMapper;
import com.group7.olineordermeal.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional  // 开启事务，确保数据库操作的一致性
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderMapper orderMapper;

    @Override
    public int cancelExpiredPendingOrders() {
        // 1. 查询所有过期的待支付订单
        List<Order> orders = orderMapper.selectExpiredPendingOrders(LocalDateTime.now());
        // 2. 遍历并修改状态为 CANCELLED
        for (Order order : orders) {
            order.setStatus(OrderStatusEnum.CANCELLED.name());
            orderMapper.updateById(order);
        }
        return orders.size();  // 返回取消的数量
    }
}