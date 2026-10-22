# 在线点餐系统 - 组员D 指导手册

> 适用角色：组员D  
> 核心职责：订单创建（从购物车生成订单）、模拟支付、用户取消订单、订单详情查询  
> **前置依赖**：组员A（用户登录）、组员B（购物车）、组员C（菜品/商家数据）已完成并合并到 `develop` 分支  
> **协作提示**：你创建的订单数据将给组员E（商家端订单管理）使用，请确保订单状态枚举与组长定义的一致。

---

## 📌 目录

1. [你的任务范围](#1-你的任务范围)
2. [开始前的准备](#2-开始前的准备)
3. [第一步：拉取最新代码并创建分支](#3-第一步拉取最新代码并创建分支)
4. [第二步：理解已有实体和枚举](#4-第二步理解已有实体和枚举)
5. [第三步：创建订单相关的 DTO](#5-第三步创建订单相关的-dto)
6. [第四步：Mapper 层（订单、订单明细）](#6-第四步mapper-层订单订单明细)
7. [第五步：Service 层（核心业务逻辑）](#7-第五步service-层核心业务逻辑)
8. [第六步：Controller 层（处理请求）](#8-第六步controller-层处理请求)
9. [第七步：前端页面（Thymeleaf + Bootstrap）](#9-第七步前端页面thymeleaf--bootstrap)
10. [第八步：测试与调试](#10-第八步测试与调试)
11. [第九步：提交代码到 Git](#11-第九步提交代码到-git)
12. [常见问题与解决方案](#12-常见问题与解决方案)

---

## 1. 你的任务范围

| 功能模块     | 说明                                                         | 依赖                                                   |
| ------------ | ------------------------------------------------------------ | ------------------------------------------------------ |
| 订单确认页   | 展示用户选择的购物车菜品、收货地址、总金额、备注             | 需要 `CartService`（组员B）、`AddressService`（组员A） |
| 创建订单     | 从购物车生成订单，价格快照，清空购物车，设置过期时间（15分钟） | 购物车数据、地址数据、菜品快照                         |
| 模拟支付     | 用户点击“模拟支付”按钮，将订单状态改为 `PAID`，记录支付时间  | 订单数据                                               |
| 用户取消订单 | 仅 `PENDING` 状态的订单可取消，取消后释放（无库存，仅改状态） | 订单数据                                               |
| 订单详情页   | 查看订单基本信息、订单明细、支付状态等                       | 订单数据                                               |

**你需要实现的前端页面**：
- `confirm_order.html` - 订单确认页（选择地址、填写备注）
- `pay_entry.html` - 支付入口页（显示订单金额、剩余支付时间）
- `order_detail.html` - 订单详情页
- `pay_success.html` - 支付成功页（可选）

**注意**：订单列表已在组员A的“我的订单”中实现，你无需重复做列表，但你需要提供 `OrderService` 供组员A调用。

---

## 2. 开始前的准备

### 2.1 确认其他组员的代码已合并
- [x] 组员A：用户登录、地址管理已完成
- [x] 组员B：购物车功能已完成
- [x] 组员C：菜品和商家数据已存在（至少一个商家、几个菜品）
- [x] 组长：订单状态枚举 `OrderStatusEnum`、订单状态机、定时任务框架已存在

### 2.2 拉取最新代码
```bash
git checkout develop
git pull origin develop
git checkout -b feature/order-pay
```

### 2.3 检查数据库表
确保以下表已存在（组长已提供）：
- `tb_order`
- `tb_order_item`

如果缺少，执行组长提供的 SQL。

---

## 3. 第二步：理解已有实体和枚举

### 3.1 订单状态枚举（组长已创建）
路径：`common/enums/OrderStatusEnum.java`

```java
public enum OrderStatusEnum {
    PENDING("待支付", 0),
    PAID("已支付", 1),
    CONFIRMED("商家已接单", 2),
    PREPARING("制作中", 3),
    DELIVERED("配送中", 4),
    COMPLETED("已完成", 5),
    CANCELLED("已取消", -1);
    // 有 getDesc(), getCode(), canCancel() 等方法
}
```

你在代码中应使用 `OrderStatusEnum.PENDING.name()` 获取字符串常量。

### 3.2 订单实体（组长已提供或你需创建）
路径：`entity/Order.java`（如果不存在则创建）

```java
package com.team.ordermeal.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class Order {
    private Long id;
    private String orderNo;          // 订单号
    private Long userId;
    private Long merchantId;
    private BigDecimal totalAmount;
    private String status;           // 对应枚举的 name
    private Long addressId;
    private String remark;
    private LocalDateTime payTime;
    private LocalDateTime confirmTime;
    private LocalDateTime deliverTime;
    private LocalDateTime completeTime;
    private LocalDateTime expireTime; // 支付过期时间
    private LocalDateTime createTime;
}
```

### 3.3 订单明细实体 `OrderItem.java`
```java
package com.team.ordermeal.entity;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class OrderItem {
    private Long id;
    private Long orderId;
    private Long dishId;
    private String dishName;   // 快照
    private BigDecimal price;  // 快照
    private Integer quantity;
    private BigDecimal subtotal;
}
```

---

## 4. 第三步：创建订单相关的 DTO

### 4.1 创建订单请求 DTO（从前端接收）
路径：`dto/CreateOrderDTO.java`

```java
package com.team.ordermeal.dto;

import lombok.Data;

@Data
public class CreateOrderDTO {
    private Long addressId;   // 选中的收货地址ID
    private String remark;    // 备注
}
```

### 4.2 支付请求 DTO
路径：`dto/PayOrderDTO.java`

```java
package com.team.ordermeal.dto;

import lombok.Data;

@Data
public class PayOrderDTO {
    private Long orderId;
}
```

---

## 5. 第四步：Mapper 层（订单、订单明细）

### 5.1 OrderMapper 接口
路径：`mapper/OrderMapper.java`（如果已存在，补充方法）

```java
package com.team.ordermeal.mapper;

import com.team.ordermeal.entity.Order;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface OrderMapper {
    int insert(Order order);
    int updateById(Order order);
    Order selectById(@Param("id") Long id);
    List<Order> selectByUserId(@Param("userId") Long userId);
    List<Order> selectByUserIdAndStatus(@Param("userId") Long userId, @Param("status") String status);
}
```

### 5.2 OrderMapper.xml
在 `src/main/resources/mapper/OrderMapper.xml` 中（如果组长已创建基础版，你只需追加方法）：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.team.ordermeal.mapper.OrderMapper">
    
    <resultMap id="BaseResultMap" type="com.team.ordermeal.entity.Order">
        <id column="id" property="id"/>
        <result column="order_no" property="orderNo"/>
        <result column="user_id" property="userId"/>
        <result column="merchant_id" property="merchantId"/>
        <result column="total_amount" property="totalAmount"/>
        <result column="status" property="status"/>
        <result column="address_id" property="addressId"/>
        <result column="remark" property="remark"/>
        <result column="pay_time" property="payTime"/>
        <result column="confirm_time" property="confirmTime"/>
        <result column="deliver_time" property="deliverTime"/>
        <result column="complete_time" property="completeTime"/>
        <result column="expire_time" property="expireTime"/>
        <result column="create_time" property="createTime"/>
    </resultMap>

    <insert id="insert" useGeneratedKeys="true" keyProperty="id">
        INSERT INTO tb_order (order_no, user_id, merchant_id, total_amount, status, address_id, remark, expire_time)
        VALUES (#{orderNo}, #{userId}, #{merchantId}, #{totalAmount}, #{status}, #{addressId}, #{remark}, #{expireTime})
    </insert>

    <update id="updateById">
        UPDATE tb_order
        SET status = #{status},
            pay_time = #{payTime},
            confirm_time = #{confirmTime},
            deliver_time = #{deliverTime},
            complete_time = #{completeTime},
            expire_time = #{expireTime}
        WHERE id = #{id}
    </update>

    <select id="selectById" resultMap="BaseResultMap">
        SELECT * FROM tb_order WHERE id = #{id}
    </select>

    <select id="selectByUserId" resultMap="BaseResultMap">
        SELECT * FROM tb_order WHERE user_id = #{userId} ORDER BY create_time DESC
    </select>

    <select id="selectByUserIdAndStatus" resultMap="BaseResultMap">
        SELECT * FROM tb_order WHERE user_id = #{userId} 
        <if test="status != null and status != ''">
            AND status = #{status}
        </if>
        ORDER BY create_time DESC
    </select>
</mapper>
```

### 5.3 OrderItemMapper 接口
路径：`mapper/OrderItemMapper.java`

```java
package com.team.ordermeal.mapper;

import com.team.ordermeal.entity.OrderItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface OrderItemMapper {
    int insert(OrderItem item);
    List<OrderItem> selectByOrderId(@Param("orderId") Long orderId);
}
```

### 5.4 OrderItemMapper.xml
在 `resources/mapper/OrderItemMapper.xml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.team.ordermeal.mapper.OrderItemMapper">
    <resultMap id="BaseResultMap" type="com.team.ordermeal.entity.OrderItem">
        <id column="id" property="id"/>
        <result column="order_id" property="orderId"/>
        <result column="dish_id" property="dishId"/>
        <result column="dish_name" property="dishName"/>
        <result column="price" property="price"/>
        <result column="quantity" property="quantity"/>
        <result column="subtotal" property="subtotal"/>
    </resultMap>

    <insert id="insert" useGeneratedKeys="true" keyProperty="id">
        INSERT INTO tb_order_item (order_id, dish_id, dish_name, price, quantity, subtotal)
        VALUES (#{orderId}, #{dishId}, #{dishName}, #{price}, #{quantity}, #{subtotal})
    </insert>

    <select id="selectByOrderId" resultMap="BaseResultMap">
        SELECT * FROM tb_order_item WHERE order_id = #{orderId}
    </select>
</mapper>
```

---

## 6. 第五步：Service 层（核心业务逻辑）

### 6.1 订单服务接口 `OrderService.java`
路径：`service/OrderService.java`

```java
package com.team.ordermeal.service;

import com.team.ordermeal.dto.CreateOrderDTO;
import com.team.ordermeal.entity.Order;
import com.team.ordermeal.entity.OrderItem;
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
}
```

### 6.2 订单服务实现类 `OrderServiceImpl.java`
路径：`service/impl/OrderServiceImpl.java`

```java
package com.team.ordermeal.service.impl;

import com.team.ordermeal.common.enums.OrderStatusEnum;
import com.team.ordermeal.common.exception.BusinessException;
import com.team.ordermeal.common.utils.OrderStateMachine;
import com.team.ordermeal.dto.CartItemDTO;
import com.team.ordermeal.dto.CreateOrderDTO;
import com.team.ordermeal.entity.Address;
import com.team.ordermeal.entity.Order;
import com.team.ordermeal.entity.OrderItem;
import com.team.ordermeal.mapper.OrderItemMapper;
import com.team.ordermeal.mapper.OrderMapper;
import com.team.ordermeal.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

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
}
```

**注意**：`AddressService` 需要有 `getAddressById` 方法，如果组员A没有提供，你需要临时添加。可以让组员A在 `AddressService` 中添加：

```java
Address getAddressById(Long id);
```

同样，`UserService` 中应有 `getUserById`。

---

## 7. 第六步：Controller 层（处理请求）

### 7.1 订单控制器 `OrderController.java`
路径：`controller/OrderController.java`

```java
package com.team.ordermeal.controller;

import com.team.ordermeal.dto.CartItemDTO;
import com.team.ordermeal.dto.CreateOrderDTO;
import com.team.ordermeal.entity.Address;
import com.team.ordermeal.entity.Order;
import com.team.ordermeal.entity.OrderItem;
import com.team.ordermeal.entity.User;
import com.team.ordermeal.service.AddressService;
import com.team.ordermeal.service.CartService;
import com.team.ordermeal.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.util.List;

@Controller
@RequestMapping("/order")
public class OrderController {

    @Autowired
    private CartService cartService;
    @Autowired
    private AddressService addressService;
    @Autowired
    private OrderService orderService;

    /**
     * 订单确认页：展示购物车商品、地址列表、总金额
     */
    @GetMapping("/confirm")
    public String confirmPage(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login";

        // 购物车商品
        List<CartItemDTO> cartItems = cartService.getCartItems(user.getId());
        if (cartItems.isEmpty()) {
            return "redirect:/cart";  // 购物车为空，跳回购物车页
        }
        BigDecimal total = cartItems.stream()
                .map(CartItemDTO::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 用户地址列表
        List<Address> addresses = addressService.listByUser(user.getId());
        // 默认地址
        Address defaultAddr = addresses.stream().filter(Address::getIsDefault).findFirst().orElse(null);

        model.addAttribute("cartItems", cartItems);
        model.addAttribute("total", total);
        model.addAttribute("addresses", addresses);
        model.addAttribute("defaultAddress", defaultAddr);
        return "confirm_order";
    }

    /**
     * 创建订单（提交确认页表单）
     */
    @PostMapping("/create")
    public String createOrder(CreateOrderDTO dto, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login";
        Order order = orderService.createOrder(user.getId(), dto);
        return "redirect:/order/pay?orderId=" + order.getId();
    }

    /**
     * 支付入口页：显示订单金额、剩余支付时间，提供模拟支付按钮
     */
    @GetMapping("/pay")
    public String payPage(@RequestParam Long orderId, HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login";
        Order order = orderService.getOrderById(orderId);
        if (order == null || !order.getUserId().equals(user.getId())) {
            return "redirect:/user/orders";
        }
        model.addAttribute("order", order);
        return "pay_entry";
    }

    /**
     * 模拟支付请求
     */
    @PostMapping("/doPay")
    public String doPay(@RequestParam Long orderId, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login";
        try {
            orderService.payOrder(orderId);
            return "redirect:/order/success?orderId=" + orderId;
        } catch (Exception e) {
            return "redirect:/order/pay?orderId=" + orderId + "&error=" + e.getMessage();
        }
    }

    /**
     * 支付成功页
     */
    @GetMapping("/success")
    public String paySuccess(@RequestParam Long orderId, HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login";
        Order order = orderService.getOrderById(orderId);
        model.addAttribute("order", order);
        return "pay_success";
    }

    /**
     * 用户取消订单（仅待支付）
     */
    @GetMapping("/cancel")
    public String cancelOrder(@RequestParam Long orderId, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login";
        try {
            orderService.cancelOrder(orderId);
        } catch (Exception e) {
            // 可记录日志
        }
        return "redirect:/user/orders";
    }

    /**
     * 订单详情页
     */
    @GetMapping("/detail/{orderId}")
    public String orderDetail(@PathVariable Long orderId, HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login";
        Order order = orderService.getOrderById(orderId);
        if (order == null || !order.getUserId().equals(user.getId())) {
            return "redirect:/user/orders";
        }
        List<OrderItem> items = orderService.getOrderItems(orderId);
        model.addAttribute("order", order);
        model.addAttribute("items", items);
        return "order_detail";
    }
}
```

---

## 8. 第七步：前端页面（Thymeleaf + Bootstrap）

所有页面放在 `src/main/resources/templates/` 下。

### 8.1 订单确认页 `confirm_order.html`

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <title>确认订单</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
</head>
<body>
<div class="container mt-4">
    <h2>确认订单</h2>
    <div class="row">
        <div class="col-md-8">
            <div class="card mb-3">
                <div class="card-header">收货地址</div>
                <div class="card-body">
                    <div th:each="addr : ${addresses}" class="form-check">
                        <input class="form-check-input" type="radio" name="addressId" th:value="${addr.id}" th:checked="${defaultAddress != null && defaultAddress.id == addr.id}" form="orderForm">
                        <label class="form-check-label">
                            <span th:text="${addr.receiverName} + ' ' + ${addr.phone}"></span><br>
                            <span th:text="${addr.province}+' '+${addr.city}+' '+${addr.district}+' '+${addr.detailAddress}"></span>
                        </label>
                    </div>
                    <a href="/user/address" class="btn btn-sm btn-link">管理地址</a>
                </div>
            </div>
            <div class="card mb-3">
                <div class="card-header">订单商品</div>
                <div class="card-body">
                    <table class="table">
                        <thead><tr><th>菜品</th><th>单价</th><th>数量</th><th>小计</th></tr></thead>
                        <tbody>
                            <tr th:each="item : ${cartItems}">
                                <td th:text="${item.dishName} + ' (' + ${item.merchantName} + ')'"></td>
                                <td th:text="'¥'+${item.price}"></td>
                                <td th:text="${item.quantity}"></td>
                                <td th:text="'¥'+${item.subtotal}"></td>
                            </tr>
                        </tbody>
                        <tfoot><tr><th colspan="3" class="text-end">总计</th><th th:text="'¥'+${total}"></th></tr></tfoot>
                    </table>
                </div>
            </div>
            <div class="card">
                <div class="card-header">备注</div>
                <div class="card-body">
                    <textarea name="remark" class="form-control" rows="2" placeholder="口味偏好等" form="orderForm"></textarea>
                </div>
            </div>
        </div>
        <div class="col-md-4">
            <div class="card">
                <div class="card-body">
                    <h5>订单金额：<span class="text-danger" th:text="'¥'+${total}"></span></h5>
                    <form id="orderForm" method="post" action="/order/create">
                        <input type="hidden" name="addressId" id="selectedAddressId">
                        <input type="hidden" name="remark" id="remark">
                        <button type="submit" class="btn btn-success w-100">提交订单</button>
                    </form>
                </div>
            </div>
        </div>
    </div>
</div>
<script>
    // 将选中的地址ID同步到隐藏域
    document.querySelectorAll('input[name="addressId"]').forEach(radio => {
        radio.addEventListener('change', function() {
            document.getElementById('selectedAddressId').value = this.value;
        });
    });
    // 如果没有默认选中，默认选中第一个
    window.onload = function() {
        let checkedRadio = document.querySelector('input[name="addressId"]:checked');
        if (checkedRadio) {
            document.getElementById('selectedAddressId').value = checkedRadio.value;
        } else {
            let first = document.querySelector('input[name="addressId"]');
            if (first) {
                first.checked = true;
                document.getElementById('selectedAddressId').value = first.value;
            }
        }
        // 备注同步
        let remarkTextarea = document.querySelector('textarea[name="remark"]');
        let remarkHidden = document.getElementById('remark');
        remarkTextarea.addEventListener('input', function() {
            remarkHidden.value = this.value;
        });
    };
</script>
</body>
</html>
```

### 8.2 支付入口页 `pay_entry.html`

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <title>订单支付</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
</head>
<body>
<div class="container mt-5" style="max-width: 600px;">
    <div class="card shadow">
        <div class="card-header bg-warning">订单支付</div>
        <div class="card-body">
            <div th:if="${param.error}" class="alert alert-danger" th:text="${param.error}"></div>
            <h4>订单号：<span th:text="${order.orderNo}"></span></h4>
            <p>订单金额：<strong class="text-danger" th:text="'¥'+${order.totalAmount}"></strong></p>
            <p>剩余支付时间：<span id="countdown" class="fw-bold"></span></p>
            <form method="post" action="/order/doPay">
                <input type="hidden" name="orderId" th:value="${order.id}">
                <button type="submit" class="btn btn-success w-100">模拟支付</button>
            </form>
            <div class="mt-3 text-center">
                <a th:href="'/order/cancel?orderId='+${order.id}" class="btn btn-link text-danger" onclick="return confirm('确定取消订单？')">取消订单</a>
            </div>
        </div>
    </div>
</div>
<script>
    // 倒计时功能
    let expireTimeStr = [[${order.expireTime}]];
    let expireTime = new Date(expireTimeStr);
    function updateCountdown() {
        let now = new Date();
        let diff = expireTime - now;
        if (diff <= 0) {
            document.getElementById('countdown').innerText = "订单已过期";
            return;
        }
        let minutes = Math.floor(diff / 60000);
        let seconds = Math.floor((diff % 60000) / 1000);
        document.getElementById('countdown').innerText = minutes + "分" + seconds + "秒";
        setTimeout(updateCountdown, 1000);
    }
    updateCountdown();
</script>
</body>
</html>
```

### 8.3 支付成功页 `pay_success.html`

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <title>支付成功</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
</head>
<body>
<div class="container mt-5 text-center">
    <div class="alert alert-success">
        <h3>支付成功！</h3>
        <p>订单号：<span th:text="${order.orderNo}"></span></p>
        <p>支付金额：¥<span th:text="${order.totalAmount}"></span></p>
    </div>
    <a href="/user/orders" class="btn btn-primary">查看我的订单</a>
    <a href="/dish/list" class="btn btn-secondary">继续点餐</a>
</div>
</body>
</html>
```

### 8.4 订单详情页 `order_detail.html`

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <title>订单详情</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
</head>
<body>
<div class="container mt-4">
    <h2>订单详情</h2>
    <div class="card">
        <div class="card-body">
            <p>订单号：<span th:text="${order.orderNo}"></span></p>
            <p>订单状态：<span th:text="${order.status}"></span></p>
            <p>下单时间：<span th:text="${order.createTime}"></span></p>
            <p>支付时间：<span th:text="${order.payTime}"></span></p>
            <p>总金额：¥<span th:text="${order.totalAmount}"></span></p>
            <p>备注：<span th:text="${order.remark}"></span></p>
            <h5>商品明细</h5>
            <table class="table table-bordered">
                <thead><tr><th>菜品名</th><th>单价</th><th>数量</th><th>小计</th></tr></thead>
                <tbody>
                    <tr th:each="item : ${items}">
                        <td th:text="${item.dishName}"></td>
                        <td th:text="'¥'+${item.price}"></td>
                        <td th:text="${item.quantity}"></td>
                        <td th:text="'¥'+${item.subtotal}"></td>
                    </tr>
                </tbody>
            </table>
            <a href="/user/orders" class="btn btn-secondary">返回订单列表</a>
        </div>
    </div>
</div>
</body>
</html>
```

---

## 9. 第八步：测试与调试

### 9.1 准备测试数据
- 确保有已登录用户（如 `user1 / 123456`）
- 确保购物车中有商品（通过组员B的页面加入）
- 确保用户有收货地址（通过组员A的地址管理添加）

### 9.2 测试流程
1. 登录用户，访问 `/cart`，确认购物车有商品。
2. 点击“去结算”，进入 `/order/confirm`。
3. 选择地址、填写备注，提交订单。
4. 跳转到 `/order/pay?orderId=xxx`，页面显示倒计时。
5. 点击“模拟支付”，跳转到支付成功页。
6. 返回“我的订单”，查看订单状态变为“已支付”。
7. 测试取消订单：创建一个新订单，在支付页点击“取消订单”，订单状态变为已取消。

### 9.3 测试过期取消
- 修改数据库订单的 `expire_time` 为过去时间，稍等片刻（组长的定时任务每30秒执行），订单状态应变为 `CANCELLED`。

---

## 10. 第九步：提交代码到 Git

```bash
git add .
git commit -m "feat(订单): 完成订单创建、支付、取消、详情功能"
git push origin feature/order-pay
```

创建 Pull Request 合并到 `develop`。

---

## 11. 常见问题与解决方案

### Q1: 创建订单时提示“购物车为空”？
- 检查是否在调用 `/order/create` 前购物车有商品。可能是提交订单后清空了购物车，但重复提交导致。

### Q2: 支付时提示“订单已过期”？
- 检查订单的 `expire_time` 是否已过。可以延长配置 `order.expire-minutes` 到更大值测试。

### Q3: 订单确认页地址无法选中？
- 检查前端 JavaScript 是否正确将选中的 radio 值赋给隐藏域 `addressId`。可以打印日志。

### Q4: 订单号生成规则需要手机号，但用户可能没有手机号？
- 注册时手机号是必填的，所以一定有。如果测试用户没有，补充手机号。

### Q5: 组员A的 `AddressService` 没有 `getAddressById` 方法？
- 临时添加：在 `AddressService` 接口和实现类中增加该方法，并通知组员A补充。

### Q6: 组员A的 `UserService` 没有 `getUserById`？
- 同样，请组员A补充。

---

**下一步**：你的订单模块完成后，组员E（商家订单管理、平台统计）和组员A（我的订单列表）将依赖你的服务。请确保你的 `OrderService` 提供 `getOrdersByUser` 方法供组员A调用。合并代码后，整个用户侧流程就闭环了。祝你顺利！