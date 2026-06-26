package com.group7.olineordermeal.service;

import com.group7.olineordermeal.dto.CreateOrderDTO;
import com.group7.olineordermeal.entity.Order;
import com.group7.olineordermeal.entity.OrderItem;
import java.util.List;

public interface OrderService {
    /**
     * 创建订单（从购物车生成）
     * @param userId 当前用户ID
     * @param dto 包含地址ID和备注
     * @return 创建的订单对象
     */
    Order createOrder(Long userId, CreateOrderDTO dto);

    /**
     * 模拟支付
     * @param orderId 订单ID
     */
    void payOrder(Long orderId);

    /**
     * 用户取消订单（仅待支付状态）
     * @param orderId 订单ID
     */
    void cancelOrder(Long orderId);

    /**
     * 根据订单ID查询订单详情（含订单明细）
     * @param orderId
     * @return 订单对象（可额外带 items 属性，但建议单独查询）
     */
    Order getOrderById(Long orderId);

    /**
     * 查询订单的明细列表
     */
    List<OrderItem> getOrderItems(Long orderId);

    /**
     * 获取用户的所有订单（供组员A使用）
     */
    List<Order> getOrdersByUser(Long userId, String status);
    /**
     * 取消超时未支付订单，返回取消条数
     *
     * @return
     */
    int cancelExpiredPendingOrders();
}