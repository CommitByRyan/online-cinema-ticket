# 在线点餐系统 - 组员E 指导手册

> 适用角色：组员E  
> 核心职责：商家订单管理（查看订单、更新状态）、平台统计报表、管理员封禁商家  
> **前置依赖**：组员A（用户）、组员B（购物车）、组员C（商家端）、组员D（订单创建与支付）已完成并合并到 `develop` 分支  
> **协作提示**：你的商家订单管理需要调用订单服务，并更新订单状态（需遵循组长定义的状态机）；管理员功能需要调用商家服务更新状态。

---

## 📌 目录

1. [你的任务范围](#1-你的任务范围)
2. [开始前的准备](#2-开始前的准备)
3. [第一步：拉取最新代码并创建分支](#3-第一步拉取最新代码并创建分支)
4. [第二步：理解已有服务和枚举](#4-第二步理解已有服务和枚举)
5. [第三步：创建 DTO（用于订单状态更新）](#5-第三步创建-dto用于订单状态更新)
6. [第四步：扩展 Mapper 层（如果需要）](#6-第四步扩展-mapper-层如果需要)
7. [第五步：Service 层（业务逻辑）](#7-第五步service-层业务逻辑)
8. [第六步：Controller 层（处理请求）](#8-第六步controller-层处理请求)
9. [第七步：前端页面（Thymeleaf + Bootstrap）](#9-第七步前端页面thymeleaf--bootstrap)
10. [第八步：测试与调试](#10-第八步测试与调试)
11. [第九步：提交代码到 Git](#11-第九步提交代码到-git)
12. [常见问题与解决方案](#12-常见问题与解决方案)

---

## 1. 你的任务范围

| 功能模块         | 说明                                                         | 依赖/协作                                                    |
| ---------------- | ------------------------------------------------------------ | ------------------------------------------------------------ |
| 商家订单列表     | 展示登录商家所收到的所有订单，按状态筛选，显示订单详情       | 需要 `OrderService` 根据 `merchantId` 查询订单（组员D需提供方法） |
| 商家更新订单状态 | 订单状态流转：`PAID` → `CONFIRMED` → `PREPARING` → `DELIVERED` → `COMPLETED`；使用组长提供的状态机校验 | 需要调用 `OrderService.updateStatus()`                       |
| 平台统计报表     | 总订单数、总交易额、活跃用户数、活跃商家数（近7天）          | 需要统计订单、用户、商家数据                                 |
| 管理员封禁商家   | 管理员查看商家列表，封禁/解封商家（修改 `tb_merchant.status` 为 `BANNED` 或 `APPROVED`） | 需要 `MerchantService.updateStatus()`                        |

**你需要实现的前端页面**：
- `merchant/order_list.html` - 商家订单列表（含状态筛选、更新状态按钮）
- `admin/merchant_list.html` - 管理员商家管理页（封禁/解封）
- `admin/platform_statistics.html` - 平台统计报表页

**注意**：组员D的 `OrderService` 目前只有按用户查询的方法，你需要添加按商家ID查询订单列表的方法。你可以自己扩展 `OrderService`，然后通知组员D合并，或者直接在你的模块中调用 `OrderMapper` 实现（推荐自己加方法，避免等待）。

---

## 2. 开始前的准备

### 2.1 确认其他组员的代码已合并
- [x] 组员A：用户、地址管理已完成
- [x] 组员B：购物车已完成
- [x] 组员C：商家注册、登录、菜品管理已完成
- [x] 组员D：订单创建、支付已完成（至少订单表有数据）
- [x] 组长：订单状态枚举、状态机工具类、定时任务已存在

### 2.2 拉取最新代码
```bash
git checkout develop
git pull origin develop
git checkout -b feature/merchant-order-admin
```

### 2.3 检查数据库表
- `tb_order` 应有测试订单（可自己先下单几条）
- `tb_merchant` 应有至少一个已审核商家
- `tb_user` 应有普通用户和管理员用户（管理员角色可自己添加字段或直接用 user 表加 role）

如果 `tb_user` 没有 `role` 字段，执行：
```sql
ALTER TABLE tb_user ADD COLUMN role VARCHAR(20) DEFAULT 'USER';
UPDATE tb_user SET role = 'ADMIN' WHERE username = 'admin';
```

---

## 3. 第二步：理解已有服务和枚举

### 3.1 订单状态枚举（组长提供）
`OrderStatusEnum` 包含：
- `PAID`（已支付）→ 商家可接单
- `CONFIRMED`（已接单）
- `PREPARING`（制作中）
- `DELIVERED`（配送中）
- `COMPLETED`（已完成）
- `CANCELLED`（已取消）

状态流转规则由 `OrderStateMachine.checkTransition(from, to)` 校验。

### 3.2 商家服务（组员C提供）
`MerchantService` 已有方法：
- `Merchant getById(Long id)`
- `void updateStatus(Long id, String status)`  ← 你需要用这个来封禁/解封

### 3.3 订单服务（组员D提供，可能需要你扩展）
组员D的 `OrderService` 目前有：
- `Order getOrderById(Long id)`
- `List<Order> getOrdersByUser(Long userId, String status)`
- `void payOrder(Long orderId)`
- `void cancelOrder(Long orderId)`

**缺少**：按商家ID查询订单、更新订单状态（商家端操作）。你需要自己添加这些方法。

---

## 4. 第三步：创建 DTO（用于订单状态更新）

### 4.1 订单状态更新 DTO
路径：`dto/OrderStatusUpdateDTO.java`

```java
package com.team.ordermeal.dto;

import lombok.Data;

@Data
public class OrderStatusUpdateDTO {
    private Long orderId;
    private String targetStatus;  // 例如 "CONFIRMED"
}
```

---

## 5. 第四步：扩展 Mapper 层（如果需要）

### 5.1 在 OrderMapper 中添加方法（如果还没有）
路径：`mapper/OrderMapper.java`

```java
// 添加以下方法
List<Order> selectByMerchantId(@Param("merchantId") Long merchantId);
List<Order> selectByMerchantIdAndStatus(@Param("merchantId") Long merchantId, @Param("status") String status);
int updateStatus(@Param("id") Long id, @Param("status") String status);
```

### 5.2 在 OrderMapper.xml 中添加对应 SQL

```xml
<select id="selectByMerchantId" resultMap="BaseResultMap">
    SELECT * FROM tb_order WHERE merchant_id = #{merchantId} ORDER BY create_time DESC
</select>

<select id="selectByMerchantIdAndStatus" resultMap="BaseResultMap">
    SELECT * FROM tb_order WHERE merchant_id = #{merchantId} 
    <if test="status != null and status != ''">
        AND status = #{status}
    </if>
    ORDER BY create_time DESC
</select>

<update id="updateStatus">
    UPDATE tb_order SET status = #{status} WHERE id = #{id}
</update>
```

> 注意：更新状态时最好同时记录时间字段（如 `confirm_time`, `deliver_time`, `complete_time`），但为了简单，你可以在 Service 层单独更新这些字段。

---

## 6. 第五步：Service 层（业务逻辑）

### 6.1 扩展 OrderService 接口（方便商家端调用）
在 `service/OrderService.java` 中添加：

```java
// 商家端订单列表
List<Order> getOrdersByMerchant(Long merchantId, String status);
// 商家更新订单状态（带状态机校验）
void updateOrderStatus(Long orderId, String targetStatus, Long merchantId);
```

### 6.2 实现类 `OrderServiceImpl` 中添加方法

```java
@Override
public List<Order> getOrdersByMerchant(Long merchantId, String status) {
    if (status != null && !status.isEmpty()) {
        return orderMapper.selectByMerchantIdAndStatus(merchantId, status);
    } else {
        return orderMapper.selectByMerchantId(merchantId);
    }
}

@Override
@Transactional
public void updateOrderStatus(Long orderId, String targetStatus, Long merchantId) {
    Order order = orderMapper.selectById(orderId);
    if (order == null) {
        throw new BusinessException("订单不存在");
    }
    // 校验商家权限
    if (!order.getMerchantId().equals(merchantId)) {
        throw new BusinessException("无权操作此订单");
    }
    // 状态机校验
    OrderStatusEnum from = OrderStatusEnum.valueOf(order.getStatus());
    OrderStatusEnum to = OrderStatusEnum.valueOf(targetStatus);
    OrderStateMachine.checkTransition(from, to);
    
    // 更新状态
    order.setStatus(targetStatus);
    // 记录对应的时间字段
    LocalDateTime now = LocalDateTime.now();
    switch (to) {
        case CONFIRMED:
            order.setConfirmTime(now);
            break;
        case PREPARING:
            // 可选，但需求没要求，可不记录
            break;
        case DELIVERED:
            order.setDeliverTime(now);
            break;
        case COMPLETED:
            order.setCompleteTime(now);
            break;
        default:
            break;
    }
    orderMapper.updateById(order);
}
```

### 6.3 统计报表 Service
路径：`service/StatisticsService.java`

```java
package com.team.ordermeal.service;

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
```

实现类 `service/impl/StatisticsServiceImpl.java`

```java
package com.team.ordermeal.service.impl;

import com.team.ordermeal.mapper.OrderMapper;
import com.team.ordermeal.mapper.UserMapper;
import com.team.ordermeal.mapper.MerchantMapper;
import com.team.ordermeal.service.StatisticsService;
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
```

> 注意：上面的方法需要你在 Mapper 中添加对应的 SQL。为节省时间，你可以直接写原生 SQL 查询，不需要新增 Mapper 方法也可以，在 Service 中用 `@Autowired` 注入 `SqlSession` 或者用 `OrderMapper` 写自定义查询。

**简化版：不新增 Mapper 方法，直接写 SQL**

```java
@Autowired
private SqlSession sqlSession;

public long getTotalOrderCount() {
    return sqlSession.selectOne("com.team.ordermeal.mapper.OrderMapper.countCompletedOrders");
}
```

但为了规范，建议在 `OrderMapper` 中添加：

```java
@Select("SELECT COUNT(*) FROM tb_order WHERE status = 'COMPLETED'")
long countCompletedOrders();

@Select("SELECT COALESCE(SUM(total_amount), 0) FROM tb_order WHERE status = 'COMPLETED'")
BigDecimal sumCompletedOrderAmount();
```

在 `UserMapper` 中添加：

```java
@Select("SELECT COUNT(DISTINCT u.id) FROM tb_user u JOIN tb_order o ON u.id = o.user_id WHERE o.status = 'COMPLETED' AND o.create_time > #{since}")
long countActiveUsersSince(@Param("since") LocalDateTime since);
```

在 `MerchantMapper` 中添加：

```java
@Select("SELECT COUNT(DISTINCT m.id) FROM tb_merchant m JOIN tb_order o ON m.id = o.merchant_id WHERE o.status = 'COMPLETED' AND o.create_time > #{since}")
long countActiveMerchantsSince(@Param("since") LocalDateTime since);
```

这些 Mapper 方法都需要在对应的接口中声明。

### 6.4 管理员封禁商家 Service（已存在 `MerchantService.updateStatus`，直接调用）

无需额外 Service。

---

## 7. 第六步：Controller 层（处理请求）

### 7.1 商家订单管理 Controller
路径：`controller/merchant/MerchantOrderController.java`

```java
package com.team.ordermeal.controller.merchant;

import com.team.ordermeal.dto.OrderStatusUpdateDTO;
import com.team.ordermeal.entity.Merchant;
import com.team.ordermeal.entity.Order;
import com.team.ordermeal.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.List;

@Controller
@RequestMapping("/merchant/order")
public class MerchantOrderController {

    @Autowired
    private OrderService orderService;

    @GetMapping("/list")
    public String list(@RequestParam(required = false) String status,
                       HttpSession session,
                       Model model) {
        Merchant merchant = (Merchant) session.getAttribute("merchant");
        if (merchant == null) return "redirect:/merchant/login";
        List<Order> orders = orderService.getOrdersByMerchant(merchant.getId(), status);
        model.addAttribute("orders", orders);
        model.addAttribute("currentStatus", status);
        return "merchant/order_list";
    }

    @PostMapping("/updateStatus")
    public String updateStatus(OrderStatusUpdateDTO dto,
                               HttpSession session) {
        Merchant merchant = (Merchant) session.getAttribute("merchant");
        if (merchant == null) return "redirect:/merchant/login";
        try {
            orderService.updateOrderStatus(dto.getOrderId(), dto.getTargetStatus(), merchant.getId());
        } catch (Exception e) {
            // 可记录日志，或者重定向带错误参数
        }
        return "redirect:/merchant/order/list";
    }
}
```

### 7.2 管理员平台统计 Controller
路径：`controller/admin/AdminStatisticsController.java`

```java
package com.team.ordermeal.controller.admin;

import com.team.ordermeal.service.StatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.math.BigDecimal;

@Controller
@RequestMapping("/admin")
public class AdminStatisticsController {

    @Autowired
    private StatisticsService statisticsService;

    @GetMapping("/statistics")
    public String statistics(Model model) {
        long totalOrders = statisticsService.getTotalOrderCount();
        BigDecimal totalRevenue = statisticsService.getTotalRevenue();
        long activeUsers = statisticsService.getActiveUserCount();
        long activeMerchants = statisticsService.getActiveMerchantCount();

        model.addAttribute("totalOrders", totalOrders);
        model.addAttribute("totalRevenue", totalRevenue);
        model.addAttribute("activeUsers", activeUsers);
        model.addAttribute("activeMerchants", activeMerchants);
        return "admin/platform_statistics";
    }
}
```

### 7.3 管理员封禁商家 Controller
路径：`controller/admin/AdminMerchantController.java`（组长可能已部分实现，你补充封禁功能）

```java
package com.team.ordermeal.controller.admin;

import com.team.ordermeal.entity.Merchant;
import com.team.ordermeal.service.MerchantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin/merchant")
public class AdminMerchantController {

    @Autowired
    private MerchantService merchantService;

    // 查看所有商家（或只查看已审核的，可加参数）
    @GetMapping("/list")
    public String list(Model model) {
        // 为了方便，查询所有商家；也可以分页
        List<Merchant> merchants = merchantService.listAll(); // 需在 MerchantService 中添加 listAll 方法
        model.addAttribute("merchants", merchants);
        return "admin/merchant_list";
    }

    // 封禁商家
    @GetMapping("/ban/{id}")
    public String ban(@PathVariable Long id) {
        merchantService.updateStatus(id, "BANNED");
        return "redirect:/admin/merchant/list";
    }

    // 解封商家（恢复为 APPROVED）
    @GetMapping("/unban/{id}")
    public String unban(@PathVariable Long id) {
        merchantService.updateStatus(id, "APPROVED");
        return "redirect:/admin/merchant/list";
    }

    // 审批入驻（组长可能已做，若没有则补充）
    @GetMapping("/approve/{id}")
    public String approve(@PathVariable Long id) {
        merchantService.updateStatus(id, "APPROVED");
        return "redirect:/admin/merchant/pending"; // 待审核列表
    }
}
```

> 注意：`MerchantService` 需要增加 `listAll()` 方法，你可以直接调用 `MerchantMapper.selectAll()`。在 `MerchantMapper` 中添加 `List<Merchant> selectAll();` 并在 XML 中实现。

---

## 8. 第七步：前端页面（Thymeleaf + Bootstrap）

### 8.1 商家订单列表 `merchant/order_list.html`

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <title>商家订单管理</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
</head>
<body>
<div class="container mt-4">
    <h2>订单管理</h2>
    <div class="mb-3">
        <a href="/merchant/order/list" class="btn btn-outline-secondary">全部</a>
        <a href="/merchant/order/list?status=PAID" class="btn btn-outline-warning">待接单</a>
        <a href="/merchant/order/list?status=CONFIRMED" class="btn btn-outline-info">已接单</a>
        <a href="/merchant/order/list?status=PREPARING" class="btn btn-outline-primary">制作中</a>
        <a href="/merchant/order/list?status=DELIVERED" class="btn btn-outline-success">配送中</a>
        <a href="/merchant/order/list?status=COMPLETED" class="btn btn-outline-secondary">已完成</a>
        <a href="/merchant/dish/list" class="btn btn-link float-end">返回菜品管理</a>
    </div>
    <table class="table table-bordered table-hover">
        <thead>
        <tr><th>订单号</th><th>用户ID</th><th>总金额</th><th>状态</th><th>下单时间</th><th>操作</th></tr>
        </thead>
        <tbody>
            <tr th:each="order : ${orders}">
                <td th:text="${order.orderNo}"></td>
                <td th:text="${order.userId}"></td>
                <td th:text="'¥'+${order.totalAmount}"></td>
                <td th:text="${order.status}"></td>
                <td th:text="${order.createTime}"></td>
                <td>
                    <form method="post" action="/merchant/order/updateStatus" style="display: inline-block;">
                        <input type="hidden" name="orderId" th:value="${order.id}">
                        <select name="targetStatus" class="form-select form-select-sm" style="width: auto; display: inline-block;">
                            <option value="PAID" th:selected="${order.status == 'PAID'}">待接单</option>
                            <option value="CONFIRMED" th:selected="${order.status == 'CONFIRMED'}">已接单</option>
                            <option value="PREPARING" th:selected="${order.status == 'PREPARING'}">制作中</option>
                            <option value="DELIVERED" th:selected="${order.status == 'DELIVERED'}">配送中</option>
                            <option value="COMPLETED" th:selected="${order.status == 'COMPLETED'}">已完成</option>
                        </select>
                        <button type="submit" class="btn btn-sm btn-primary">更新状态</button>
                    </form>
                    <a th:href="'/order/detail/'+${order.id}" class="btn btn-sm btn-info">详情</a>
                </td>
            </tr>
            <tr th:if="${orders.isEmpty()}">
                <td colspan="6" class="text-center">暂无订单</td>
            </tr>
        </tbody>
    </table>
</div>
</body>
</html>
```

### 8.2 管理员商家列表 `admin/merchant_list.html`

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <title>商家管理</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
</head>
<body>
<div class="container mt-4">
    <h2>商家管理</h2>
    <table class="table table-bordered">
        <thead>
        <tr><th>ID</th><th>用户名</th><th>店铺名称</th><th>法人</th><th>电话</th><th>状态</th><th>营业状态</th><th>操作</th></tr>
        </thead>
        <tbody>
            <tr th:each="m : ${merchants}">
                <td th:text="${m.id}"></td>
                <td th:text="${m.username}"></td>
                <td th:text="${m.merchantName}"></td>
                <td th:text="${m.legalPerson}"></td>
                <td th:text="${m.phone}"></td>
                <td th:text="${m.status}"></td>
                <td th:text="${m.businessStatus}"></td>
                <td>
                    <a th:if="${m.status != 'BANNED'}" th:href="'/admin/merchant/ban/'+${m.id}" class="btn btn-sm btn-danger" onclick="return confirm('封禁该商家？')">封禁</a>
                    <a th:if="${m.status == 'BANNED'}" th:href="'/admin/merchant/unban/'+${m.id}" class="btn btn-sm btn-success" onclick="return confirm('解封该商家？')">解封</a>
                </td>
            </tr>
        </tbody>
    </table>
    <a href="/admin/statistics" class="btn btn-primary">平台统计</a>
</div>
</body>
</html>
```

### 8.3 平台统计报表 `admin/platform_statistics.html`

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <title>平台统计</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
</head>
<body>
<div class="container mt-4">
    <h2>平台运营统计</h2>
    <div class="row">
        <div class="col-md-6">
            <div class="card text-white bg-primary mb-3">
                <div class="card-header">总订单数</div>
                <div class="card-body">
                    <h1 class="card-title" th:text="${totalOrders}">0</h1>
                </div>
            </div>
        </div>
        <div class="col-md-6">
            <div class="card text-white bg-success mb-3">
                <div class="card-header">总交易额</div>
                <div class="card-body">
                    <h1 class="card-title" th:text="'¥'+${totalRevenue}">0</h1>
                </div>
            </div>
        </div>
        <div class="col-md-6">
            <div class="card text-white bg-info mb-3">
                <div class="card-header">活跃用户数（近7天）</div>
                <div class="card-body">
                    <h1 class="card-title" th:text="${activeUsers}">0</h1>
                </div>
            </div>
        </div>
        <div class="col-md-6">
            <div class="card text-white bg-warning mb-3">
                <div class="card-header">活跃商家数（近7天）</div>
                <div class="card-body">
                    <h1 class="card-title" th:text="${activeMerchants}">0</h1>
                </div>
            </div>
        </div>
    </div>
    <a href="/admin/merchant/list" class="btn btn-secondary">返回商家管理</a>
</div>
</body>
</html>
```

---

## 9. 第八步：测试与调试

### 9.1 准备测试数据
- 使用普通用户（`user1`）下单至少一笔，并完成支付（通过组员D的支付流程）。
- 确保订单状态为 `PAID`。

### 9.2 商家端订单管理测试
- 登录商家账号（如 `merchant1 / 123456`）。
- 访问 `/merchant/order/list`，应看到订单列表。
- 点击“更新状态”：将订单从 `PAID` → `CONFIRMED` → `PREPARING` → `DELIVERED` → `COMPLETED`，每一步都应成功。
- 尝试非法流转（如从 `COMPLETED` 再修改为 `DELIVERED`），应报错。

### 9.3 管理员封禁商家测试
- 登录管理员账号（需要事先在 `tb_user` 中设置 `role='ADMIN'`，如 `admin / 123456`）。
- 访问 `/admin/merchant/list`，选择商家点击“封禁”。
- 再以该商家账号登录，应提示“账号未审核通过或被封禁”。

### 9.4 平台统计测试
- 管理员访问 `/admin/statistics`，查看数据是否与订单实际情况一致。

---

## 10. 第九步：提交代码到 Git

```bash
git add .
git commit -m "feat(商家订单管理&管理员): 完成商家订单状态流转、管理员封禁商家、平台统计报表"
git push origin feature/merchant-order-admin
```

创建 Pull Request 合并到 `develop`。

---

## 11. 常见问题与解决方案

### Q1: 订单列表页面不显示订单？
- 检查是否以商家身份登录，且该商家确实有订单（可自己用用户端下单测试）。
- 检查 `OrderService.getOrdersByMerchant` 方法是否正确实现，SQL 是否正确。

### Q2: 更新订单状态时提示“不允许流转”？
- 检查状态机规则：`PAID` 只能转为 `CONFIRMED` 或 `CANCELLED`；`CONFIRMED` 只能转 `PREPARING`；等等。确保下拉菜单的选项顺序正确。

### Q3: 管理员登录后无法访问 `/admin` 路径？
- 需要在 `User` 实体中有 `role` 字段，并在登录时存入 session，然后在拦截器中判断。为简化演示，你可以直接在管理员控制器上不做权限校验，但建议组长添加一个简单的拦截器或使用 `@SessionAttribute` 判断。

### Q4: 统计报表数据不准确？
- 确认订单状态为 `COMPLETED` 的订单金额求和是否正确。可以手动执行 SQL 验证。

### Q5: 商家订单状态更新后，订单列表没有刷新？
- 重定向正确，一般没问题。如果遇到，检查 Controller 返回的视图名称是否正确。

---

## 12. 最终整合说明

你的模块完成后，整个系统的主要功能闭环如下：

- **用户端**：注册登录 → 浏览菜品 → 加入购物车 → 确认订单 → 支付 → 查看订单
- **商家端**：注册 → 等待审核 → 登录 → 菜品管理 → 查看订单 → 更新订单状态
- **管理员**：审核商家入驻 → 封禁商家 → 查看平台统计

组长会负责最终的代码合并、定时任务、全局异常处理等。

**最后提醒**：你的工作依赖于其他组员的模块，如果遇到某个依赖方法不存在，请及时与对应组员沟通补充。祝你顺利完成！