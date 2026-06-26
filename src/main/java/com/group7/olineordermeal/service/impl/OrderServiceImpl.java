package com.group7.olineordermeal.service.impl;

import com.group7.olineordermeal.common.enums.OrderStatusEnum;
import com.group7.olineordermeal.common.exception.BusinessException;
import com.group7.olineordermeal.dto.CartItemDTO;
import com.group7.olineordermeal.dto.CreateOrderDTO;
import com.group7.olineordermeal.entity.Address;
import com.group7.olineordermeal.entity.Order;
import com.group7.olineordermeal.entity.OrderItem;
import com.group7.olineordermeal.mapper.OrderItemMapper;
import com.group7.olineordermeal.mapper.OrderMapper;
import com.group7.olineordermeal.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderItemMapper orderItemMapper;
    @Autowired
    private CartService cartService;        // 组员B提供
    @Autowired
    private AddressService addressService;  // 组员A提供
    @Autowired
    private UserService userService;        // 组员A提供，用于获取手机号生成订单号

    @Value("${order.expire-minutes:15}")
    private int expireMinutes;  // 从配置文件读取过期时间

    // 生成订单号：手机号后4位 + yyyyMMddHHmmss + 4位随机数
    private String generateOrderNo(Long userId) {
        String phone = userService.getUserById(userId).getPhone();
        String phoneSuffix = phone.length() >= 4 ? phone.substring(phone.length() - 4) : "0000";
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        int random = (int) (Math.random() * 10000);
        return phoneSuffix + timestamp + String.format("%04d", random);
    }

    @Override
    @Transactional
    public Order createOrder(Long userId, CreateOrderDTO dto) {

        // 1. 获取购物车商品列表
        List<CartItemDTO> cartItems = cartService.getCartItems(userId);
        if (cartItems.isEmpty()) {
            throw new BusinessException("购物车为空，无法下单");
        }
        // 2. 获取收货地址（校验）
        Address address = addressService.getAddressById(dto.getAddressId());
        if (address == null || !address.getUserId().equals(userId)) {
            throw new BusinessException("收货地址无效");
        }
        // 3. 获取商家ID（购物车中所有商品属于同一商家，取第一个）
        Long merchantId = cartItems.get(0).getMerchantId();
        // 4. 计算总金额（价格快照：使用购物车中的价格，该价格是菜品表中的当前价格，已经由cartService提供）
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (CartItemDTO item : cartItems) {
            totalAmount = totalAmount.add(item.getSubtotal());
        }
        // 5. 创建订单
        Order order = new Order();
        order.setOrderNo(generateOrderNo(userId));
        order.setUserId(userId);
        order.setMerchantId(merchantId);
        order.setTotalAmount(totalAmount);
        order.setStatus(OrderStatusEnum.PENDING.name());
        order.setAddressId(dto.getAddressId());
        order.setRemark(dto.getRemark());
        order.setExpireTime(LocalDateTime.now().plusMinutes(expireMinutes));
        orderMapper.insert(order);
        // 6. 创建订单明细（快照）
        for (CartItemDTO item : cartItems) {
            OrderItem orderItem = new OrderItem();
            orderItem.setOrderId(order.getId());
            orderItem.setDishId(item.getDishId());
            orderItem.setDishName(item.getDishName());
            orderItem.setPrice(item.getPrice());
            orderItem.setQuantity(item.getQuantity());
            orderItem.setSubtotal(item.getSubtotal());
            orderItemMapper.insert(orderItem);
        }
        // 7. 清空购物车
        cartService.clearCart(userId);
        return order;
    }

    @Override
    @Transactional
    public void payOrder(Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (!OrderStatusEnum.PENDING.name().equals(order.getStatus())) {
            throw new BusinessException("订单状态不允许支付");
        }
        // 检查是否过期
        if (order.getExpireTime().isBefore(LocalDateTime.now())) {
            throw new BusinessException("订单已过期，请重新下单");
        }
        // 支付：修改状态、记录支付时间
        order.setStatus(OrderStatusEnum.PAID.name());
        order.setPayTime(LocalDateTime.now());
        orderMapper.updateById(order);
        // 注意：这里不需要释放任何资源，因为不涉及库存
    }

    @Override
    @Transactional
    public void cancelOrder(Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        OrderStatusEnum from = OrderStatusEnum.valueOf(order.getStatus());
        if (!from.canCancel()) {  // 利用枚举中的 canCancel() 方法
            throw new BusinessException("当前状态无法取消订单");
        }
        order.setStatus(OrderStatusEnum.CANCELLED.name());
        orderMapper.updateById(order);
        // 注意：无需释放座位/库存
    }

    @Override
    public Order getOrderById(Long orderId) {
        return orderMapper.selectById(orderId);
    }

    @Override
    public List<OrderItem> getOrderItems(Long orderId) {
        return orderItemMapper.selectByOrderId(orderId);
    }

    @Override
    public List<Order> getOrdersByUser(Long userId, String status) {
        if (status != null && !status.isEmpty()) {
            return orderMapper.selectByUserIdAndStatus(userId, status);
        } else {
            return orderMapper.selectByUserId(userId);
        }
    }
    @Override
    public int cancelExpiredPendingOrders() {
        // 原有业务逻辑
        return 0;
    }
}