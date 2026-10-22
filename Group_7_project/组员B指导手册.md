# 在线点餐系统 - 组员B 指导手册

> 适用角色：组员B  
> 核心职责：菜品浏览、搜索、购物车（前端+后端）  
> **前置依赖**：组长已完成项目基础架构、数据库、公共类；组员A已完成用户登录（你可以在浏览器先登录，获得 session）  
> **协作提示**：你的购物车需要知道当前登录用户（从 session 获取），菜品数据来自组员C的商家后台（如果组员C还没做完，你可以先手动在数据库插入一些测试菜品）

---

## 📌 目录

1. [你的任务范围](#1-你的任务范围)
2. [开始前的准备](#2-开始前的准备)
3. [第一步：拉取最新代码并创建分支](#3-第一步拉取最新代码并创建分支)
4. [第二步：创建实体类和 DTO](#4-第二步创建实体类和-dto)
5. [第三步：Mapper 层（数据库操作）](#5-第三步mapper-层数据库操作)
6. [第四步：Service 层（业务逻辑）](#6-第四步service-层业务逻辑)
7. [第五步：Controller 层（处理请求）](#7-第五步controller-层处理请求)
8. [第六步：前端页面（Thymeleaf + Bootstrap）](#8-第六步前端页面thymeleaf--bootstrap)
9. [第七步：测试与调试](#9-第七步测试与调试)
10. [第八步：提交代码到 Git](#10-第八步提交代码到-git)
11. [常见问题与解决方案](#11-常见问题与解决方案)

---

## 1. 你的任务范围

| 功能模块     | 说明                                                         | 依赖                                                  |
| ------------ | ------------------------------------------------------------ | ----------------------------------------------------- |
| 菜品分页列表 | 展示菜品图片、名称、价格、商家名称，支持分页                 | 需要菜品数据（组员C负责插入，或你自己先插入测试数据） |
| 多条件搜索   | 按菜品名、价格区间、商家名、分类搜索                         | 同上                                                  |
| 购物车       | 添加菜品、修改数量、删除、清空、实时计算总价，**强制单商家** | 需要用户登录（组员A已完成）                           |
| 购物车页面   | 展示已选菜品列表，支持继续购物或去结算                       | 订单创建按钮跳转到组员D的订单确认页                   |

**你需要实现的前端页面**：
- `dish_list.html` - 菜品浏览/搜索页（卡片式布局）
- `cart.html` - 购物车页面

---

## 2. 开始前的准备

### 2.1 确认组长和组员A已完成以下内容
- [x] 项目可运行，`application.properties` 配置正确
- [x] 数据库表 `tb_dish`、`tb_merchant`、`tb_cart` 已创建（组长已提供 SQL）
- [x] 公共类 `Result`、`BusinessException`、`GlobalExceptionHandler` 存在
- [x] 已有测试用户（可用组员A注册的账号，或数据库中 `user1 / 123456`）

### 2.2 准备测试数据（如果组员C还没开始）
在数据库中手动插入至少 2 个商家和几个菜品，以便你开发时能看到数据：

```sql
-- 确保有商家（状态为 APPROVED）
INSERT INTO tb_merchant (username, password, merchant_name, legal_person, phone, address, business_license, status) 
VALUES ('test_merchant', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOpw7Kc4n1jfC', '测试餐厅', '李四', '13800138000', '测试路1号', '12345678', 'APPROVED');

-- 插入菜品
INSERT INTO tb_dish (merchant_id, dish_name, price, image, status, sales) VALUES
(1, '宫保鸡丁', 38.00, '/images/gongbao.jpg', 'ONLINE', 120),
(1, '水煮鱼', 68.00, '/images/shuizhuyu.jpg', 'ONLINE', 85),
(1, '麻婆豆腐', 22.00, '/images/mapo.jpg', 'ONLINE', 200),
(1, '酸辣土豆丝', 16.00, '/images/tudou.jpg', 'ONLINE', 150);
```

> 图片路径可以先不真实存在，后续可以放一个默认图片。

---

## 3. 第一步：拉取最新代码并创建分支

```bash
# 进入项目目录
cd online-ordermeal

# 切换到 develop 分支（组长已经合并了组员A的代码）
git checkout develop
git pull origin develop

# 创建你的功能分支
git checkout -b feature/dish-cart
```

用 IDEA 打开项目，确保能正常启动。

---

## 4. 第二步：创建实体类和 DTO

在 `com.team.ordermeal.entity` 下创建（如果已存在则跳过）：

### 4.1 菜品实体 `Dish.java`

```java
package com.team.ordermeal.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 菜品实体，对应表 tb_dish
 */
@Data
public class Dish {
    private Long id;
    private Long merchantId;      // 所属商家ID
    private Long categoryId;      // 分类ID（可为空）
    private String dishName;      // 菜品名
    private BigDecimal price;     // 售价
    private BigDecimal originalPrice; // 原价
    private String image;         // 图片路径
    private String description;   // 描述
    private String status;        // ONLINE / OFFLINE
    private Integer sales;        // 销量
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    
    // 注意：为了展示方便，可以额外加一个字段 merchantName，但不在数据库表中
    // 可以在查询时用联表查询填充
}
```

### 4.2 购物车实体 `Cart.java`

```java
package com.team.ordermeal.entity;

import lombok.Data;

/**
 * 购物车实体，对应表 tb_cart
 * 注意：购物车只存用户ID、菜品ID、数量，其他信息（价格、名称）需要关联查询
 */
@Data
public class Cart {
    private Long id;
    private Long userId;
    private Long dishId;
    private Integer quantity;
}
```

### 4.3 辅助 DTO（用于前端展示购物车时携带菜品详情）

路径：`dto/CartItemDTO.java`

```java
package com.team.ordermeal.dto;

import lombok.Data;
import java.math.BigDecimal;

/**
 * 购物车展示项（包含菜品信息和数量）
 */
@Data
public class CartItemDTO {
    private Long dishId;
    private String dishName;
    private BigDecimal price;
    private String image;
    private Integer quantity;
    private BigDecimal subtotal;   // price * quantity
    private Long merchantId;
    private String merchantName;
}
```

### 4.4 搜索条件 DTO

路径：`dto/DishSearchDTO.java`

```java
package com.team.ordermeal.dto;

import lombok.Data;
import java.math.BigDecimal;

/**
 * 菜品搜索条件
 */
@Data
public class DishSearchDTO {
    private String dishName;      // 菜品名（模糊）
    private BigDecimal minPrice;  // 最低价
    private BigDecimal maxPrice;  // 最高价
    private String merchantName;  // 商家名（模糊）
    private String category;      // 分类（预留，若需要）
}
```

---

## 5. 第三步：Mapper 层（数据库操作）

### 5.1 DishMapper 接口
路径：`mapper/DishMapper.java`

```java
package com.team.ordermeal.mapper;

import com.team.ordermeal.entity.Dish;
import com.team.ordermeal.dto.DishSearchDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface DishMapper {

    /**
     * 根据ID查询菜品
     */
    Dish selectById(@Param("id") Long id);

    /**
     * 多条件分页查询菜品（联表查询商家名称）
     * @param search 搜索条件
     * @return 菜品列表，每个Dish对象会额外包含 merchantName 字段（可以用扩展对象或直接用Dish加一个临时属性）
     */
    List<Dish> searchWithMerchant(@Param("search") DishSearchDTO search);
    
    /**
     * 批量查询菜品（用于购物车展示）
     */
    List<Dish> selectByIds(@Param("ids") List<Long> ids);
}
```

> 因为 Dish 实体中没有 merchantName，我们可以新建一个扩展类，但为了方便，可以在 XML 中用 `resultMap` 自动映射到 Dish 的额外属性。更简单的方法：直接在 SQL 中返回 `dish.*, m.merchant_name as merchantName`，然后在 Dish 类中增加一个 `merchantName` 字段（非数据库字段）。我推荐加字段。

修改 `Dish.java`，增加：
```java
private String merchantName;  // 临时字段，不在数据库表中
```

### 5.2 DishMapper.xml
在 `src/main/resources/mapper/` 下创建 `DishMapper.xml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.team.ordermeal.mapper.DishMapper">

    <resultMap id="BaseResultMap" type="com.team.ordermeal.entity.Dish">
        <id column="id" property="id"/>
        <result column="merchant_id" property="merchantId"/>
        <result column="category_id" property="categoryId"/>
        <result column="dish_name" property="dishName"/>
        <result column="price" property="price"/>
        <result column="original_price" property="originalPrice"/>
        <result column="image" property="image"/>
        <result column="description" property="description"/>
        <result column="status" property="status"/>
        <result column="sales" property="sales"/>
        <result column="create_time" property="createTime"/>
        <result column="update_time" property="updateTime"/>
        <result column="merchant_name" property="merchantName"/>  <!-- 额外字段 -->
    </resultMap>

    <!-- 基础查询字段 -->
    <sql id="BaseColumns">
        d.id, d.merchant_id, d.category_id, d.dish_name, d.price, d.original_price,
        d.image, d.description, d.status, d.sales, d.create_time, d.update_time,
        m.merchant_name
    </sql>

    <!-- 多条件搜索（只查上架的菜品） -->
    <select id="searchWithMerchant" resultMap="BaseResultMap">
        SELECT <include refid="BaseColumns"/>
        FROM tb_dish d
        LEFT JOIN tb_merchant m ON d.merchant_id = m.id
        WHERE d.status = 'ONLINE'
        <if test="search.dishName != null and search.dishName != ''">
            AND d.dish_name LIKE CONCAT('%', #{search.dishName}, '%')
        </if>
        <if test="search.minPrice != null">
            AND d.price >= #{search.minPrice}
        </if>
        <if test="search.maxPrice != null">
            AND d.price &lt;= #{search.maxPrice}
        </if>
        <if test="search.merchantName != null and search.merchantName != ''">
            AND m.merchant_name LIKE CONCAT('%', #{search.merchantName}, '%')
        </if>
        ORDER BY d.sales DESC, d.create_time DESC
    </select>

    <select id="selectById" resultMap="BaseResultMap">
        SELECT <include refid="BaseColumns"/>
        FROM tb_dish d
        LEFT JOIN tb_merchant m ON d.merchant_id = m.id
        WHERE d.id = #{id}
    </select>

    <!-- 批量查询 -->
    <select id="selectByIds" resultMap="BaseResultMap">
        SELECT <include refid="BaseColumns"/>
        FROM tb_dish d
        LEFT JOIN tb_merchant m ON d.merchant_id = m.id
        WHERE d.id IN
        <foreach collection="ids" item="id" open="(" separator="," close=")">
            #{id}
        </foreach>
    </select>

</mapper>
```

### 5.3 CartMapper 接口
路径：`mapper/CartMapper.java`

```java
package com.team.ordermeal.mapper;

import com.team.ordermeal.entity.Cart;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface CartMapper {

    /**
     * 查询某用户的所有购物车记录
     */
    List<Cart> selectByUserId(@Param("userId") Long userId);

    /**
     * 查询某用户在某个菜品上的记录
     */
    Cart selectByUserIdAndDishId(@Param("userId") Long userId, @Param("dishId") Long dishId);

    /**
     * 插入购物车记录
     */
    int insert(Cart cart);

    /**
     * 更新数量
     */
    int updateQuantity(@Param("id") Long id, @Param("quantity") Integer quantity);

    /**
     * 删除某条购物车记录
     */
    int deleteById(@Param("id") Long id);

    /**
     * 清空某用户的所有购物车
     */
    int deleteByUserId(@Param("userId") Long userId);

    /**
     * 根据用户ID和菜品ID删除（用于数量减到0时）
     */
    int deleteByUserIdAndDishId(@Param("userId") Long userId, @Param("dishId") Long dishId);
}
```

### 5.4 CartMapper.xml
在 `src/main/resources/mapper/` 下创建 `CartMapper.xml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.team.ordermeal.mapper.CartMapper">

    <resultMap id="BaseResultMap" type="com.team.ordermeal.entity.Cart">
        <id column="id" property="id"/>
        <result column="user_id" property="userId"/>
        <result column="dish_id" property="dishId"/>
        <result column="quantity" property="quantity"/>
    </resultMap>

    <select id="selectByUserId" resultMap="BaseResultMap">
        SELECT * FROM tb_cart WHERE user_id = #{userId}
    </select>

    <select id="selectByUserIdAndDishId" resultMap="BaseResultMap">
        SELECT * FROM tb_cart WHERE user_id = #{userId} AND dish_id = #{dishId}
    </select>

    <insert id="insert" useGeneratedKeys="true" keyProperty="id">
        INSERT INTO tb_cart (user_id, dish_id, quantity)
        VALUES (#{userId}, #{dishId}, #{quantity})
        ON DUPLICATE KEY UPDATE quantity = quantity + #{quantity}
    </insert>

    <update id="updateQuantity">
        UPDATE tb_cart SET quantity = #{quantity} WHERE id = #{id}
    </update>

    <delete id="deleteById">
        DELETE FROM tb_cart WHERE id = #{id}
    </delete>

    <delete id="deleteByUserId">
        DELETE FROM tb_cart WHERE user_id = #{userId}
    </delete>

    <delete id="deleteByUserIdAndDishId">
        DELETE FROM tb_cart WHERE user_id = #{userId} AND dish_id = #{dishId}
    </delete>

</mapper>
```

> 注意：`insert` 语句中使用了 `ON DUPLICATE KEY UPDATE`，这要求表 `tb_cart` 有唯一索引 `(user_id, dish_id)`。组长建表时已经加了 `UNIQUE KEY uk_user_dish`，所以可以这样用。

---

## 6. 第四步：Service 层（业务逻辑）

### 6.1 DishService 接口
路径：`service/DishService.java`

```java
package com.team.ordermeal.service;

import com.team.ordermeal.entity.Dish;
import com.team.ordermeal.dto.DishSearchDTO;
import com.github.pagehelper.PageInfo;
import java.util.List;

public interface DishService {
    /**
     * 分页搜索菜品
     */
    PageInfo<Dish> searchDishPage(DishSearchDTO search, int pageNum, int pageSize);
    
    /**
     * 根据ID获取菜品详情
     */
    Dish getDishDetail(Long dishId);
    
    /**
     * 批量获取菜品（用于购物车）
     */
    List<Dish> getDishesByIds(List<Long> ids);
}
```

### 6.2 DishServiceImpl 实现类
路径：`service/impl/DishServiceImpl.java`

```java
package com.team.ordermeal.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.team.ordermeal.dto.DishSearchDTO;
import com.team.ordermeal.entity.Dish;
import com.team.ordermeal.mapper.DishMapper;
import com.team.ordermeal.service.DishService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class DishServiceImpl implements DishService {

    @Autowired
    private DishMapper dishMapper;

    @Override
    public PageInfo<Dish> searchDishPage(DishSearchDTO search, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<Dish> list = dishMapper.searchWithMerchant(search);
        return new PageInfo<>(list);
    }

    @Override
    public Dish getDishDetail(Long dishId) {
        return dishMapper.selectById(dishId);
    }

    @Override
    public List<Dish> getDishesByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) return List.of();
        return dishMapper.selectByIds(ids);
    }
}
```

### 6.3 CartService 接口
路径：`service/CartService.java`

```java
package com.team.ordermeal.service;

import com.team.ordermeal.dto.CartItemDTO;
import java.util.List;

public interface CartService {
    /**
     * 添加菜品到购物车（如果已存在则增加数量）
     */
    void addToCart(Long userId, Long dishId, Integer quantity);
    
    /**
     * 修改购物车中某个菜品的数量
     */
    void updateQuantity(Long userId, Long dishId, Integer quantity);
    
    /**
     * 删除购物车中的某个菜品
     */
    void removeFromCart(Long userId, Long dishId);
    
    /**
     * 清空购物车
     */
    void clearCart(Long userId);
    
    /**
     * 获取购物车列表（带菜品详情）
     */
    List<CartItemDTO> getCartItems(Long userId);
    
    /**
     * 获取购物车中商家的ID（用于强制单商家校验）
     */
    Long getCartMerchantId(Long userId);
}
```

### 6.4 CartServiceImpl 实现类
路径：`service/impl/CartServiceImpl.java`

```java
package com.team.ordermeal.service.impl;

import com.team.ordermeal.common.exception.BusinessException;
import com.team.ordermeal.dto.CartItemDTO;
import com.team.ordermeal.entity.Cart;
import com.team.ordermeal.entity.Dish;
import com.team.ordermeal.mapper.CartMapper;
import com.team.ordermeal.mapper.DishMapper;
import com.team.ordermeal.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private CartMapper cartMapper;
    
    @Autowired
    private DishMapper dishMapper;

    @Override
    @Transactional
    public void addToCart(Long userId, Long dishId, Integer quantity) {
        // 1. 查询要添加的菜品
        Dish dish = dishMapper.selectById(dishId);
        if (dish == null || !"ONLINE".equals(dish.getStatus())) {
            throw new BusinessException("菜品不存在或已下架");
        }
        // 2. 检查购物车是否已有其他商家的菜品（强制单商家）
        Long existingMerchantId = getCartMerchantId(userId);
        if (existingMerchantId != null && !existingMerchantId.equals(dish.getMerchantId())) {
            throw new BusinessException("购物车已有其他商家的菜品，请先清空购物车或完成下单");
        }
        // 3. 插入或更新购物车（Mapper 中使用了 ON DUPLICATE KEY UPDATE）
        Cart cart = new Cart();
        cart.setUserId(userId);
        cart.setDishId(dishId);
        cart.setQuantity(quantity);
        cartMapper.insert(cart);
    }

    @Override
    @Transactional
    public void updateQuantity(Long userId, Long dishId, Integer quantity) {
        if (quantity <= 0) {
            removeFromCart(userId, dishId);
            return;
        }
        Cart cart = cartMapper.selectByUserIdAndDishId(userId, dishId);
        if (cart == null) {
            throw new BusinessException("购物车中不存在该菜品");
        }
        cart.setQuantity(quantity);
        cartMapper.updateQuantity(cart.getId(), quantity);
    }

    @Override
    @Transactional
    public void removeFromCart(Long userId, Long dishId) {
        cartMapper.deleteByUserIdAndDishId(userId, dishId);
    }

    @Override
    @Transactional
    public void clearCart(Long userId) {
        cartMapper.deleteByUserId(userId);
    }

    @Override
    public List<CartItemDTO> getCartItems(Long userId) {
        List<Cart> carts = cartMapper.selectByUserId(userId);
        if (carts.isEmpty()) {
            return List.of();
        }
        // 批量查询菜品信息
        List<Long> dishIds = carts.stream().map(Cart::getDishId).collect(Collectors.toList());
        List<Dish> dishes = dishMapper.selectByIds(dishIds);
        Map<Long, Dish> dishMap = dishes.stream().collect(Collectors.toMap(Dish::getId, d -> d));
        
        List<CartItemDTO> result = new ArrayList<>();
        for (Cart cart : carts) {
            Dish dish = dishMap.get(cart.getDishId());
            if (dish == null) continue;
            CartItemDTO item = new CartItemDTO();
            item.setDishId(dish.getId());
            item.setDishName(dish.getDishName());
            item.setPrice(dish.getPrice());
            item.setImage(dish.getImage());
            item.setQuantity(cart.getQuantity());
            item.setSubtotal(dish.getPrice().multiply(BigDecimal.valueOf(cart.getQuantity())));
            item.setMerchantId(dish.getMerchantId());
            item.setMerchantName(dish.getMerchantName());
            result.add(item);
        }
        return result;
    }

    @Override
    public Long getCartMerchantId(Long userId) {
        List<Cart> carts = cartMapper.selectByUserId(userId);
        if (carts.isEmpty()) return null;
        // 取第一个菜品的商家ID（因为强制单商家，所有菜品属于同一商家）
        Long dishId = carts.get(0).getDishId();
        Dish dish = dishMapper.selectById(dishId);
        return dish != null ? dish.getMerchantId() : null;
    }
}
```

---

## 7. 第五步：Controller 层（处理请求）

### 7.1 DishController（菜品浏览、搜索）
路径：`controller/DishController.java`

```java
package com.team.ordermeal.controller;

import com.github.pagehelper.PageInfo;
import com.team.ordermeal.dto.DishSearchDTO;
import com.team.ordermeal.entity.Dish;
import com.team.ordermeal.service.DishService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class DishController {

    @Autowired
    private DishService dishService;

    /**
     * 菜品列表页（支持搜索）
     */
    @GetMapping("/dish/list")
    public String dishList(DishSearchDTO search,
                           @RequestParam(defaultValue = "1") int pageNum,
                           @RequestParam(defaultValue = "8") int pageSize,
                           Model model) {
        PageInfo<Dish> pageInfo = dishService.searchDishPage(search, pageNum, pageSize);
        model.addAttribute("page", pageInfo);
        model.addAttribute("search", search);
        return "dish_list";
    }

    /**
     * 菜品详情（可选，用于点击详情按钮）
     */
    @GetMapping("/dish/detail")
    public String dishDetail(@RequestParam Long id, Model model) {
        Dish dish = dishService.getDishDetail(id);
        model.addAttribute("dish", dish);
        return "dish_detail";
    }
}
```

### 7.2 CartController（购物车操作）
路径：`controller/CartController.java`

```java
package com.team.ordermeal.controller;

import com.team.ordermeal.common.result.Result;
import com.team.ordermeal.dto.CartItemDTO;
import com.team.ordermeal.entity.User;
import com.team.ordermeal.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.util.List;

@Controller
@RequestMapping("/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    /**
     * 查看购物车页面
     */
    @GetMapping
    public String cartPage(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        List<CartItemDTO> items = cartService.getCartItems(user.getId());
        BigDecimal total = items.stream()
                .map(CartItemDTO::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        model.addAttribute("items", items);
        model.addAttribute("total", total);
        return "cart";
    }

    /**
     * 添加菜品到购物车（AJAX 或 重定向）
     * 这里提供两种方式：前端用 form 或 异步。我们使用重定向，简单。
     */
    @PostMapping("/add")
    public String addToCart(@RequestParam Long dishId,
                            @RequestParam(defaultValue = "1") Integer quantity,
                            HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        try {
            cartService.addToCart(user.getId(), dishId, quantity);
        } catch (Exception e) {
            // 错误时可以在页面显示，这里简单重定向并带参数
            return "redirect:/dish/list?error=" + e.getMessage();
        }
        return "redirect:/cart";  // 添加后跳转到购物车页
    }

    /**
     * 更新购物车数量（AJAX 或 表单，这里用 POST 重定向）
     */
    @PostMapping("/update")
    public String updateQuantity(@RequestParam Long dishId,
                                 @RequestParam Integer quantity,
                                 HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login";
        cartService.updateQuantity(user.getId(), dishId, quantity);
        return "redirect:/cart";
    }

    /**
     * 删除购物车菜品
     */
    @GetMapping("/remove")
    public String remove(@RequestParam Long dishId, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login";
        cartService.removeFromCart(user.getId(), dishId);
        return "redirect:/cart";
    }

    /**
     * 清空购物车
     */
    @GetMapping("/clear")
    public String clear(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login";
        cartService.clearCart(user.getId());
        return "redirect:/cart";
    }
}
```

> 注意：为了统一，购物车的修改操作使用了重定向，没有使用 AJAX。如果你想实现实时更新总价而不刷新页面，可以用 AJAX，但会增加复杂度。本手册采用简单重定向方式，完全够用。

---

## 8. 第六步：前端页面（Thymeleaf + Bootstrap）

### 8.1 菜品列表页 `dish_list.html`
放在 `src/main/resources/templates/` 下。

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>在线点餐 - 菜品浏览</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <style>
        .card-img-top { height: 180px; object-fit: cover; }
    </style>
</head>
<body>
<div class="container mt-3">
    <!-- 导航栏 -->
    <nav class="navbar navbar-expand-lg navbar-light bg-light rounded mb-3">
        <div class="container-fluid">
            <a class="navbar-brand" href="/dish/list">在线点餐</a>
            <div class="ms-auto">
                <a href="/cart" class="btn btn-outline-primary">购物车 🛒</a>
                <a href="/user/profile" class="btn btn-outline-secondary ms-2">个人中心</a>
            </div>
        </div>
    </nav>

    <!-- 搜索表单 -->
    <div class="card mb-4">
        <div class="card-body">
            <form method="get" action="/dish/list" class="row g-3">
                <div class="col-md-3">
                    <label class="form-label">菜品名</label>
                    <input type="text" name="dishName" class="form-control" th:value="${search.dishName}">
                </div>
                <div class="col-md-2">
                    <label class="form-label">最低价</label>
                    <input type="number" step="0.01" name="minPrice" class="form-control" th:value="${search.minPrice}">
                </div>
                <div class="col-md-2">
                    <label class="form-label">最高价</label>
                    <input type="number" step="0.01" name="maxPrice" class="form-control" th:value="${search.maxPrice}">
                </div>
                <div class="col-md-3">
                    <label class="form-label">商家名</label>
                    <input type="text" name="merchantName" class="form-control" th:value="${search.merchantName}">
                </div>
                <div class="col-md-2 d-flex align-items-end">
                    <button type="submit" class="btn btn-primary w-100">搜索</button>
                </div>
            </form>
        </div>
    </div>

    <!-- 错误提示 -->
    <div th:if="${param.error}" class="alert alert-danger" th:text="${param.error}"></div>

    <!-- 菜品卡片列表 -->
    <div class="row row-cols-1 row-cols-md-2 row-cols-lg-4 g-4">
        <div th:each="dish : ${page.list}" class="col">
            <div class="card h-100 shadow-sm">
                <img th:src="${dish.image != null ? dish.image : '/images/default.jpg'}" class="card-img-top" alt="菜品图片">
                <div class="card-body">
                    <h5 class="card-title" th:text="${dish.dishName}">菜品名</h5>
                    <p class="card-text text-danger fw-bold" th:text="'¥' + ${dish.price}">价格</p>
                    <p class="card-text small text-muted" th:text="${dish.merchantName}">商家名</p>
                </div>
                <div class="card-footer bg-white border-top-0">
                    <form method="post" action="/cart/add">
                        <input type="hidden" name="dishId" th:value="${dish.id}">
                        <div class="input-group">
                            <input type="number" name="quantity" value="1" min="1" max="10" class="form-control form-control-sm" style="width: 70px;">
                            <button type="submit" class="btn btn-sm btn-success">加入购物车</button>
                        </div>
                    </form>
                    <a th:href="'/dish/detail?id='+${dish.id}" class="btn btn-link btn-sm mt-2">查看详情</a>
                </div>
            </div>
        </div>
        <div th:if="${page.list.isEmpty()}" class="col-12 text-center">
            <p class="text-muted">暂无菜品，请尝试其他搜索条件。</p>
        </div>
    </div>

    <!-- 分页 -->
    <nav aria-label="Page navigation" class="mt-4">
        <ul class="pagination justify-content-center">
            <li class="page-item" th:classappend="${page.hasPreviousPage()} ? '' : 'disabled'">
                <a class="page-link" th:href="@{/dish/list(pageNum=${page.prePage}, dishName=${search.dishName}, minPrice=${search.minPrice}, maxPrice=${search.maxPrice}, merchantName=${search.merchantName})}">上一页</a>
            </li>
            <li th:each="i : ${#numbers.sequence(1, page.pages)}" class="page-item" th:classappend="${page.pageNum == i} ? 'active' : ''">
                <a class="page-link" th:href="@{/dish/list(pageNum=${i}, dishName=${search.dishName}, minPrice=${search.minPrice}, maxPrice=${search.maxPrice}, merchantName=${search.merchantName})}" th:text="${i}"></a>
            </li>
            <li class="page-item" th:classappend="${page.hasNextPage()} ? '' : 'disabled'">
                <a class="page-link" th:href="@{/dish/list(pageNum=${page.nextPage}, dishName=${search.dishName}, minPrice=${search.minPrice}, maxPrice=${search.maxPrice}, merchantName=${search.merchantName})}">下一页</a>
            </li>
        </ul>
    </nav>
</div>
</body>
</html>
```

### 8.2 购物车页面 `cart.html`

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <title>我的购物车</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
</head>
<body>
<div class="container mt-4">
    <h2>购物车</h2>
    <a href="/dish/list" class="btn btn-secondary mb-3">继续点餐</a>
    <a href="/cart/clear" class="btn btn-danger mb-3 float-end" onclick="return confirm('确定清空购物车？')">清空购物车</a>
    
    <table class="table table-bordered">
        <thead>
            <tr>
                <th>菜品</th><th>单价</th><th>数量</th><th>小计</th><th>操作</th>
            </tr>
        </thead>
        <tbody>
            <tr th:each="item : ${items}">
                <td th:text="${item.dishName} + ' (' + ${item.merchantName} + ')'"></td>
                <td th:text="'¥' + ${item.price}"></td>
                <td>
                    <form method="post" action="/cart/update" style="display: inline-block;">
                        <input type="hidden" name="dishId" th:value="${item.dishId}">
                        <input type="number" name="quantity" th:value="${item.quantity}" min="0" style="width: 70px;" onchange="this.form.submit()">
                    </form>
                </td>
                <td th:text="'¥' + ${item.subtotal}"></td>
                <td>
                    <a th:href="'/cart/remove?dishId='+${item.dishId}" class="btn btn-sm btn-danger">删除</a>
                </td>
            </tr>
            <tr th:if="${items.isEmpty()}">
                <td colspan="5" class="text-center">购物车空空如也，去<a href="/dish/list">点餐</a>吧</td>
            </tr>
        </tbody>
        <tfoot th:if="not ${items.isEmpty()}">
            <tr>
                <th colspan="3" class="text-end">总计：</th>
                <th th:text="'¥' + ${total}"></th>
                <th>
                    <a href="/order/confirm" class="btn btn-success">去结算</a>
                </th>
            </tr>
        </tfoot>
    </table>
</div>
</body>
</html>
```

> 注意：`/order/confirm` 是组员D负责的订单确认页，暂时可以先留空或跳转到一个提示页面。

### 8.3 菜品详情页（可选）
`dish_detail.html` 简单展示，不赘述。

---

## 9. 第七步：测试与调试

### 9.1 手动测试步骤
1. 启动项目：`mvn spring-boot:run`
2. 先访问 `/login`，用 `user1 / 123456` 登录（组员A已实现）
3. 登录成功后应该自动跳转到 `/dish/list`（如果组员A的登录页跳转正确）
4. 在菜品列表页：
   - 查看卡片是否正常显示（图片可能404，没关系）
   - 输入搜索条件（如菜品名“鸡丁”），点击搜索，看结果
   - 点击某个菜品的“加入购物车”，填写数量，提交
5. 跳转到购物车页面 `/cart`，检查：
   - 列表显示刚添加的菜品
   - 修改数量（输入新数字后回车或自动提交表单）
   - 删除菜品
   - 清空购物车
   - 购物车总价计算正确
6. 测试强制单商家：
   - 先加入商家A的菜品，再加入商家B的菜品，应该会报错（页面提示错误，重定向回列表页）
   - 清空购物车后再加商家B的正常

### 9.2 常见问题调试
- **图片不显示**：把测试图片放到 `src/main/resources/static/images/` 下，并在数据库 `image` 字段中写 `/images/xxx.jpg`。
- **分页插件报错**：确保 `pom.xml` 中有 `pagehelper-spring-boot-starter`，且 `MyBatisConfig` 正确配置。
- **购物车添加后没有反应**：检查 `CartController.addToCart` 方法是否捕获异常，并重定向带错误信息。可以打断点调试。

---

## 10. 第八步：提交代码到 Git

```bash
git add .
git commit -m "feat(菜品&购物车): 实现菜品列表、搜索、购物车功能"
git push origin feature/dish-cart
```

然后在 Gitee 创建 Pull Request 合并到 `develop` 分支，通知组长。

---

## 11. 常见问题与解决方案

### Q1: 启动后访问 `/dish/list` 报 500，提示 `DishMapper not found`
- 检查 `DishMapper` 接口是否有 `@Mapper` 注解。
- 检查 `application.properties` 中 `mybatis.mapper-locations` 是否包含 `classpath:mapper/*.xml`。
- 检查 `DishMapper.xml` 中 `namespace` 是否与接口全限定名一致。

### Q2: 购物车添加时报错“购物车已有其他商家的菜品”
- 这是正常业务逻辑，说明你测试时加了不同商家的菜品。你可以先清空购物车再试。

### Q3: 购物车数量修改后不自动刷新
- 在 `cart.html` 中，`<input>` 的 `onchange="this.form.submit()"` 需要确保表单的 `method="post"` 且 `action` 正确。如果浏览器不支持，也可以改成手动点击“更新”按钮，但为了简单，这样已经够用。

### Q4: 分页链接中搜索条件丢失
- 在分页的 `<a>` 标签中，我们手动添加了 `dishName`、`minPrice` 等参数，确保它们正确传递。如果参数很多，可以改用 Thymeleaf 的 `@{}` 表达式动态获取当前请求参数，但手动写也可以。

### Q5: 购物车总价计算不精确
- 使用 `BigDecimal` 来避免浮点误差，已在代码中使用 `BigDecimal`。

---

**下一步**：当你完成购物车功能后，组员D将依赖你的购物车数据来创建订单。你们可以协商约定：组员D调用你的 `CartService.getCartItems()` 来获取购物车内容。你的代码合并后，组员D就可以开始他的任务了。祝你顺利！