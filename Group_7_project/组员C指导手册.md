# 在线点餐系统 - 组员C 指导手册

> 适用角色：组员C  
> 核心职责：商家端（注册、登录、店铺信息管理、菜品分类管理、菜品管理）  
> **前置依赖**：组长已完成项目基础架构、数据库设计、公共类；商家注册后需管理员（组长）审核才能登录  
> **协作提示**：你的菜品数据会被组员B（菜品浏览）使用；你的商家登录状态会用在后面订单管理（组员E）。请确保接口规范。

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

| 功能模块     | 说明                                        | 依赖/协作                           |
| ------------ | ------------------------------------------- | ----------------------------------- |
| 商家注册     | 提交店铺信息，状态为 `PENDING`（待审核）    | 管理员（组长）审批后变为 `APPROVED` |
| 商家登录     | 用户名+密码，检查状态是否为 `APPROVED`      | 依赖管理员审核                      |
| 店铺信息管理 | 编辑店铺基本信息、营业状态（营业中/休息中） | 登录后操作                          |
| 菜品分类管理 | 增删改查分类（每个商家独立）                | 用于菜品挂载                        |
| 菜品管理     | 增删改查、上架/下架、上传图片（路径）       | 数据供组员B展示                     |

**你需要实现的前端页面**（放在 `src/main/resources/templates/merchant/` 下）：
- `merchant_register.html` - 商家注册页
- `merchant_login.html` - 商家登录页
- `merchant_info.html` - 店铺信息管理
- `category_list.html` - 分类列表（管理）
- `dish_list.html` - 菜品列表（管理）
- `dish_form.html` - 新增/编辑菜品表单

---

## 2. 开始前的准备

### 2.1 确认组长已完成以下工作
- [x] 数据库表 `tb_merchant`、`tb_dish_category`、`tb_dish` 已创建（见组长 SQL）
- [x] 公共类、密码加密、异常处理存在
- [x] 项目可启动，访问 `http://localhost:8080` 正常

### 2.2 创建测试商家（用于开发登录调试）
你可以在数据库中手动插入一个已审核的商家，方便测试登录后的功能：
```sql
-- 密码 123456 的 BCrypt 密文
INSERT INTO tb_merchant (username, password, merchant_name, legal_person, phone, address, business_license, status) 
VALUES ('testshop', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOpw7Kc4n1jfC', '测试餐厅', '张三', '13812345678', '测试路88号', '123456789012345', 'APPROVED');
```

> 注意：实际业务中商家注册后状态为 `PENDING`，需要管理员审批。你可以先把这个商家的状态改为 `APPROVED` 来测试登录和后续功能。最后演示时再演示完整流程。

---

## 3. 第一步：拉取最新代码并创建分支

```bash
git checkout develop
git pull origin develop
git checkout -b feature/merchant
```

在 IDEA 中打开项目，确认可以运行。

---

## 4. 第二步：创建实体类和 DTO

### 4.1 商家实体 `Merchant.java`（可能已由组长创建，如果没有则创建）
路径：`entity/Merchant.java`

```java
package com.team.ordermeal.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 商家实体，对应表 tb_merchant
 */
@Data
public class Merchant {
    private Long id;
    private String username;
    private String password;
    private String merchantName;    // 店铺名称
    private String legalPerson;     // 法人
    private String phone;
    private String address;
    private String businessLicense;
    private String status;          // PENDING, APPROVED, BANNED
    private String businessStatus;  // OPEN, CLOSED
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
```

### 4.2 菜品分类实体 `DishCategory.java`
路径：`entity/DishCategory.java`

```java
package com.team.ordermeal.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class DishCategory {
    private Long id;
    private Long merchantId;
    private String categoryName;
    private Integer sortOrder;      // 排序序号
    private LocalDateTime createTime;
}
```

### 4.3 菜品实体 `Dish.java`（如果组员B已创建，你可以复用；为保独立，确保存在）
已由组员B创建，但你可以检查是否有字段完整。若需要，可补充 `merchantName` 字段（非数据库）用于展示，但非必须。

### 4.4 DTO 类（用于表单提交）

**商家注册 DTO**：`dto/MerchantRegisterDTO.java`

```java
package com.team.ordermeal.dto;

import lombok.Data;

@Data
public class MerchantRegisterDTO {
    private String username;
    private String password;
    private String merchantName;
    private String legalPerson;
    private String phone;
    private String address;
    private String businessLicense;
}
```

**店铺信息更新 DTO**：`dto/MerchantUpdateDTO.java`

```java
package com.team.ordermeal.dto;

import lombok.Data;

@Data
public class MerchantUpdateDTO {
    private String merchantName;
    private String legalPerson;
    private String phone;
    private String address;
    private String businessLicense;
    private String businessStatus;  // OPEN / CLOSED
}
```

**菜品 DTO**：`dto/DishDTO.java`（用于新增/编辑）

```java
package com.team.ordermeal.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class DishDTO {
    private Long id;               // 编辑时才有id
    private Long categoryId;
    private String dishName;
    private BigDecimal price;
    private BigDecimal originalPrice;
    private String image;
    private String description;
    private String status;         // ONLINE / OFFLINE
}
```

**分类 DTO**：`dto/CategoryDTO.java`

```java
package com.team.ordermeal.dto;

import lombok.Data;

@Data
public class CategoryDTO {
    private Long id;
    private String categoryName;
    private Integer sortOrder;
}
```

---

## 5. 第三步：Mapper 层（数据库操作）

### 5.1 MerchantMapper（可能已由组长创建，但你可以补充方法）
路径：`mapper/MerchantMapper.java`

```java
package com.team.ordermeal.mapper;

import com.team.ordermeal.entity.Merchant;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface MerchantMapper {
    Merchant selectByUsername(@Param("username") String username);
    Merchant selectById(@Param("id") Long id);
    int insert(Merchant merchant);
    int updateById(Merchant merchant);
}
```

对应的 `MerchantMapper.xml`（在 `resources/mapper/` 下）已由组长提供基础版，你可以检查。如果没有，复制以下：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.team.ordermeal.mapper.MerchantMapper">
    <resultMap id="BaseResultMap" type="com.team.ordermeal.entity.Merchant">
        <id column="id" property="id"/>
        <result column="username" property="username"/>
        <result column="password" property="password"/>
        <result column="merchant_name" property="merchantName"/>
        <result column="legal_person" property="legalPerson"/>
        <result column="phone" property="phone"/>
        <result column="address" property="address"/>
        <result column="business_license" property="businessLicense"/>
        <result column="status" property="status"/>
        <result column="business_status" property="businessStatus"/>
        <result column="create_time" property="createTime"/>
        <result column="update_time" property="updateTime"/>
    </resultMap>

    <select id="selectByUsername" resultMap="BaseResultMap">
        SELECT * FROM tb_merchant WHERE username = #{username}
    </select>

    <select id="selectById" resultMap="BaseResultMap">
        SELECT * FROM tb_merchant WHERE id = #{id}
    </select>

    <insert id="insert" useGeneratedKeys="true" keyProperty="id">
        INSERT INTO tb_merchant (username, password, merchant_name, legal_person, phone, address, business_license, status)
        VALUES (#{username}, #{password}, #{merchantName}, #{legalPerson}, #{phone}, #{address}, #{businessLicense}, 'PENDING')
    </insert>

    <update id="updateById">
        UPDATE tb_merchant
        SET merchant_name = #{merchantName},
            legal_person = #{legalPerson},
            phone = #{phone},
            address = #{address},
            business_license = #{businessLicense},
            business_status = #{businessStatus},
            update_time = NOW()
        WHERE id = #{id}
    </update>
</mapper>
```

### 5.2 DishCategoryMapper
路径：`mapper/DishCategoryMapper.java`

```java
package com.team.ordermeal.mapper;

import com.team.ordermeal.entity.DishCategory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface DishCategoryMapper {
    List<DishCategory> selectByMerchantId(@Param("merchantId") Long merchantId);
    DishCategory selectById(@Param("id") Long id);
    int insert(DishCategory category);
    int updateById(DishCategory category);
    int deleteById(@Param("id") Long id);
}
```

`resources/mapper/DishCategoryMapper.xml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.team.ordermeal.mapper.DishCategoryMapper">
    <resultMap id="BaseResultMap" type="com.team.ordermeal.entity.DishCategory">
        <id column="id" property="id"/>
        <result column="merchant_id" property="merchantId"/>
        <result column="category_name" property="categoryName"/>
        <result column="sort_order" property="sortOrder"/>
        <result column="create_time" property="createTime"/>
    </resultMap>

    <select id="selectByMerchantId" resultMap="BaseResultMap">
        SELECT * FROM tb_dish_category WHERE merchant_id = #{merchantId} ORDER BY sort_order ASC
    </select>

    <select id="selectById" resultMap="BaseResultMap">
        SELECT * FROM tb_dish_category WHERE id = #{id}
    </select>

    <insert id="insert" useGeneratedKeys="true" keyProperty="id">
        INSERT INTO tb_dish_category (merchant_id, category_name, sort_order)
        VALUES (#{merchantId}, #{categoryName}, #{sortOrder})
    </insert>

    <update id="updateById">
        UPDATE tb_dish_category
        SET category_name = #{categoryName}, sort_order = #{sortOrder}
        WHERE id = #{id}
    </update>

    <delete id="deleteById">
        DELETE FROM tb_dish_category WHERE id = #{id}
    </delete>
</mapper>
```

### 5.3 DishMapper（组员B已创建，但你可能需要额外的方法：比如商家端按商家ID查询菜品列表）
在 `DishMapper` 中添加方法：

```java
// 在 DishMapper.java 中添加
List<Dish> selectByMerchantId(@Param("merchantId") Long merchantId);
```

在 `DishMapper.xml` 中添加：

```xml
<select id="selectByMerchantId" resultMap="BaseResultMap">
    SELECT <include refid="BaseColumns"/>
    FROM tb_dish d
    LEFT JOIN tb_merchant m ON d.merchant_id = m.id
    WHERE d.merchant_id = #{merchantId}
    ORDER BY d.create_time DESC
</select>
```

另外，组员B的 `DishMapper.xml` 中已经定义了 `BaseColumns` 和 `BaseResultMap`，所以你直接复用即可。注意不要重复定义导致冲突。

如果组员B还没有提交代码，你可以先自己创建这些方法，等组员B合并后若冲突再解决。建议你先在本地开发，后续合并时组长会处理冲突。

---

## 6. 第四步：Service 层（业务逻辑）

### 6.1 MerchantService（商家注册、登录、信息修改）
路径：`service/MerchantService.java`

```java
package com.team.ordermeal.service;

import com.team.ordermeal.dto.MerchantRegisterDTO;
import com.team.ordermeal.dto.MerchantUpdateDTO;
import com.team.ordermeal.entity.Merchant;

public interface MerchantService {
    void register(MerchantRegisterDTO dto);
    Merchant login(String username, String rawPassword);
    Merchant getById(Long id);
    void updateInfo(Long id, MerchantUpdateDTO dto);
}
```

实现类 `service/impl/MerchantServiceImpl.java`

```java
package com.team.ordermeal.service.impl;

import com.team.ordermeal.common.exception.BusinessException;
import com.team.ordermeal.common.utils.PasswordEncoderUtil;
import com.team.ordermeal.dto.MerchantRegisterDTO;
import com.team.ordermeal.dto.MerchantUpdateDTO;
import com.team.ordermeal.entity.Merchant;
import com.team.ordermeal.mapper.MerchantMapper;
import com.team.ordermeal.service.MerchantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MerchantServiceImpl implements MerchantService {

    @Autowired
    private MerchantMapper merchantMapper;

    @Override
    public void register(MerchantRegisterDTO dto) {
        // 检查用户名是否已存在
        if (merchantMapper.selectByUsername(dto.getUsername()) != null) {
            throw new BusinessException("用户名已存在");
        }
        Merchant merchant = new Merchant();
        merchant.setUsername(dto.getUsername());
        merchant.setPassword(PasswordEncoderUtil.encode(dto.getPassword()));
        merchant.setMerchantName(dto.getMerchantName());
        merchant.setLegalPerson(dto.getLegalPerson());
        merchant.setPhone(dto.getPhone());
        merchant.setAddress(dto.getAddress());
        merchant.setBusinessLicense(dto.getBusinessLicense());
        // 状态默认为 PENDING，等待管理员审核
        merchant.setStatus("PENDING");
        merchant.setBusinessStatus("OPEN"); // 默认营业中
        merchantMapper.insert(merchant);
    }

    @Override
    public Merchant login(String username, String rawPassword) {
        Merchant merchant = merchantMapper.selectByUsername(username);
        if (merchant == null) {
            return null; // 用户名不存在，外层跳转到注册
        }
        if (!PasswordEncoderUtil.matches(rawPassword, merchant.getPassword())) {
            throw new BusinessException("密码错误");
        }
        // 检查状态
        if (!"APPROVED".equals(merchant.getStatus())) {
            throw new BusinessException("账号未审核通过或被封禁");
        }
        return merchant;
    }

    @Override
    public Merchant getById(Long id) {
        return merchantMapper.selectById(id);
    }

    @Override
    public void updateInfo(Long id, MerchantUpdateDTO dto) {
        Merchant merchant = merchantMapper.selectById(id);
        if (merchant == null) {
            throw new BusinessException("商家不存在");
        }
        merchant.setMerchantName(dto.getMerchantName());
        merchant.setLegalPerson(dto.getLegalPerson());
        merchant.setPhone(dto.getPhone());
        merchant.setAddress(dto.getAddress());
        merchant.setBusinessLicense(dto.getBusinessLicense());
        merchant.setBusinessStatus(dto.getBusinessStatus());
        merchantMapper.updateById(merchant);
    }
}
```

### 6.2 DishCategoryService（分类管理）
路径：`service/DishCategoryService.java`

```java
package com.team.ordermeal.service;

import com.team.ordermeal.dto.CategoryDTO;
import com.team.ordermeal.entity.DishCategory;
import java.util.List;

public interface DishCategoryService {
    List<DishCategory> listByMerchant(Long merchantId);
    void addCategory(Long merchantId, CategoryDTO dto);
    void updateCategory(Long id, CategoryDTO dto);
    void deleteCategory(Long id, Long merchantId);
}
```

实现类 `service/impl/DishCategoryServiceImpl.java`

```java
package com.team.ordermeal.service.impl;

import com.team.ordermeal.common.exception.BusinessException;
import com.team.ordermeal.dto.CategoryDTO;
import com.team.ordermeal.entity.DishCategory;
import com.team.ordermeal.mapper.DishCategoryMapper;
import com.team.ordermeal.service.DishCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class DishCategoryServiceImpl implements DishCategoryService {

    @Autowired
    private DishCategoryMapper categoryMapper;

    @Override
    public List<DishCategory> listByMerchant(Long merchantId) {
        return categoryMapper.selectByMerchantId(merchantId);
    }

    @Override
    public void addCategory(Long merchantId, CategoryDTO dto) {
        DishCategory category = new DishCategory();
        category.setMerchantId(merchantId);
        category.setCategoryName(dto.getCategoryName());
        category.setSortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : 0);
        categoryMapper.insert(category);
    }

    @Override
    public void updateCategory(Long id, CategoryDTO dto) {
        DishCategory category = categoryMapper.selectById(id);
        if (category == null) {
            throw new BusinessException("分类不存在");
        }
        category.setCategoryName(dto.getCategoryName());
        category.setSortOrder(dto.getSortOrder());
        categoryMapper.updateById(category);
    }

    @Override
    public void deleteCategory(Long id, Long merchantId) {
        DishCategory category = categoryMapper.selectById(id);
        if (category == null || !category.getMerchantId().equals(merchantId)) {
            throw new BusinessException("分类不存在或无权限");
        }
        // TODO: 检查该分类下是否有菜品，若有则应提示先转移或删除菜品
        categoryMapper.deleteById(id);
    }
}
```

### 6.3 DishService（商家端菜品管理）
组员B已提供 `DishService`，但你可以扩展商家端专用的方法（如 `addDish`, `updateDish`, `deleteDish`, `listByMerchant`）。建议新建一个 `MerchantDishService` 接口，避免与组员B的冲突。

路径：`service/MerchantDishService.java`

```java
package com.team.ordermeal.service;

import com.team.ordermeal.dto.DishDTO;
import com.team.ordermeal.entity.Dish;
import java.util.List;

public interface MerchantDishService {
    List<Dish> listByMerchant(Long merchantId);
    Dish getDish(Long id);
    void addDish(Long merchantId, DishDTO dto);
    void updateDish(Long id, DishDTO dto);
    void deleteDish(Long id, Long merchantId);
    void updateStatus(Long id, String status, Long merchantId);
}
```

实现类 `service/impl/MerchantDishServiceImpl.java`

```java
package com.team.ordermeal.service.impl;

import com.team.ordermeal.common.exception.BusinessException;
import com.team.ordermeal.dto.DishDTO;
import com.team.ordermeal.entity.Dish;
import com.team.ordermeal.mapper.DishMapper;
import com.team.ordermeal.service.MerchantDishService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class MerchantDishServiceImpl implements MerchantDishService {

    @Autowired
    private DishMapper dishMapper;

    @Override
    public List<Dish> listByMerchant(Long merchantId) {
        return dishMapper.selectByMerchantId(merchantId);
    }

    @Override
    public Dish getDish(Long id) {
        return dishMapper.selectById(id);
    }

    @Override
    @Transactional
    public void addDish(Long merchantId, DishDTO dto) {
        Dish dish = new Dish();
        dish.setMerchantId(merchantId);
        dish.setCategoryId(dto.getCategoryId());
        dish.setDishName(dto.getDishName());
        dish.setPrice(dto.getPrice());
        dish.setOriginalPrice(dto.getOriginalPrice());
        dish.setImage(dto.getImage());
        dish.setDescription(dto.getDescription());
        dish.setStatus(dto.getStatus() != null ? dto.getStatus() : "ONLINE");
        dish.setSales(0);
        // 注意：DishMapper 的 insert 方法需要你确认是否存在，可能组员B已定义，如果没有就加一下
        // 我们假设 DishMapper 有 insert 方法，如果没有，请添加。
        // 因为之前组员B的DishMapper没有insert，你需要自己补充
        // 在 DishMapper.java 中添加 int insert(Dish dish);
        // 在 DishMapper.xml 中添加 insert 语句
        dishMapper.insert(dish);
    }

    @Override
    @Transactional
    public void updateDish(Long id, DishDTO dto) {
        Dish dish = dishMapper.selectById(id);
        if (dish == null) {
            throw new BusinessException("菜品不存在");
        }
        dish.setCategoryId(dto.getCategoryId());
        dish.setDishName(dto.getDishName());
        dish.setPrice(dto.getPrice());
        dish.setOriginalPrice(dto.getOriginalPrice());
        dish.setImage(dto.getImage());
        dish.setDescription(dto.getDescription());
        dish.setStatus(dto.getStatus());
        dishMapper.updateById(dish);
    }

    @Override
    @Transactional
    public void deleteDish(Long id, Long merchantId) {
        Dish dish = dishMapper.selectById(id);
        if (dish == null || !dish.getMerchantId().equals(merchantId)) {
            throw new BusinessException("菜品不存在或无权限");
        }
        dishMapper.deleteById(id); // 需要在 DishMapper 中添加 deleteById 方法
    }

    @Override
    @Transactional
    public void updateStatus(Long id, String status, Long merchantId) {
        Dish dish = dishMapper.selectById(id);
        if (dish == null || !dish.getMerchantId().equals(merchantId)) {
            throw new BusinessException("菜品不存在或无权限");
        }
        dish.setStatus(status);
        dishMapper.updateById(dish);
    }
}
```

> 注意：上面的代码用到了 `dishMapper.insert`、`dishMapper.updateById`、`dishMapper.deleteById` 等方法，这些可能组员B没有定义。你需要补充到 `DishMapper` 接口和 XML 中。为了不破坏组员B的代码，你可以在你的分支上添加这些方法，合并时组长会协调。

**补充 DishMapper 方法**（在你的分支上添加）：

```java
// 在 DishMapper.java 中添加
int insert(Dish dish);
int updateById(Dish dish);
int deleteById(@Param("id") Long id);
```

在 `DishMapper.xml` 中添加：

```xml
<insert id="insert" useGeneratedKeys="true" keyProperty="id">
    INSERT INTO tb_dish (merchant_id, category_id, dish_name, price, original_price, image, description, status, sales)
    VALUES (#{merchantId}, #{categoryId}, #{dishName}, #{price}, #{originalPrice}, #{image}, #{description}, #{status}, #{sales})
</insert>

<update id="updateById">
    UPDATE tb_dish
    SET category_id = #{categoryId},
        dish_name = #{dishName},
        price = #{price},
        original_price = #{originalPrice},
        image = #{image},
        description = #{description},
        status = #{status},
        update_time = NOW()
    WHERE id = #{id}
</update>

<delete id="deleteById">
    DELETE FROM tb_dish WHERE id = #{id}
</delete>
```

---

## 7. 第五步：Controller 层（处理请求）

### 7.1 MerchantAuthController（商家登录、注册）
路径：`controller/merchant/MerchantAuthController.java`

```java
package com.team.ordermeal.controller.merchant;

import com.team.ordermeal.dto.MerchantRegisterDTO;
import com.team.ordermeal.entity.Merchant;
import com.team.ordermeal.service.MerchantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpSession;

@Controller
@RequestMapping("/merchant")
public class MerchantAuthController {

    @Autowired
    private MerchantService merchantService;

    @GetMapping("/login")
    public String loginPage() {
        return "merchant/merchant_login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String username,
                        @RequestParam String password,
                        HttpSession session,
                        Model model) {
        try {
            Merchant merchant = merchantService.login(username, password);
            if (merchant == null) {
                return "redirect:/merchant/register";
            }
            session.setAttribute("merchant", merchant);
            return "redirect:/merchant/dish/list"; // 跳转到菜品管理页
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("showFindPwd", true);
            return "merchant/merchant_login";
        }
    }

    @GetMapping("/register")
    public String registerPage() {
        return "merchant/merchant_register";
    }

    @PostMapping("/register")
    public String register(MerchantRegisterDTO dto, Model model) {
        try {
            merchantService.register(dto);
            return "redirect:/merchant/login?msg=注册成功，等待管理员审核";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "merchant/merchant_register";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.removeAttribute("merchant");
        return "redirect:/merchant/login";
    }
}
```

### 7.2 MerchantInfoController（店铺信息管理）
路径：`controller/merchant/MerchantInfoController.java`

```java
package com.team.ordermeal.controller.merchant;

import com.team.ordermeal.dto.MerchantUpdateDTO;
import com.team.ordermeal.entity.Merchant;
import com.team.ordermeal.service.MerchantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpSession;

@Controller
@RequestMapping("/merchant/info")
public class MerchantInfoController {

    @Autowired
    private MerchantService merchantService;

    @GetMapping
    public String infoPage(HttpSession session, Model model) {
        Merchant merchant = (Merchant) session.getAttribute("merchant");
        if (merchant == null) return "redirect:/merchant/login";
        model.addAttribute("merchant", merchant);
        return "merchant/merchant_info";
    }

    @PostMapping("/update")
    public String updateInfo(MerchantUpdateDTO dto, HttpSession session, Model model) {
        Merchant merchant = (Merchant) session.getAttribute("merchant");
        if (merchant == null) return "redirect:/merchant/login";
        try {
            merchantService.updateInfo(merchant.getId(), dto);
            // 更新 session 中的信息
            merchant.setMerchantName(dto.getMerchantName());
            merchant.setLegalPerson(dto.getLegalPerson());
            merchant.setPhone(dto.getPhone());
            merchant.setAddress(dto.getAddress());
            merchant.setBusinessLicense(dto.getBusinessLicense());
            merchant.setBusinessStatus(dto.getBusinessStatus());
            session.setAttribute("merchant", merchant);
            model.addAttribute("success", "更新成功");
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
        }
        model.addAttribute("merchant", merchant);
        return "merchant/merchant_info";
    }
}
```

### 7.3 MerchantCategoryController（菜品分类管理）
路径：`controller/merchant/MerchantCategoryController.java`

```java
package com.team.ordermeal.controller.merchant;

import com.team.ordermeal.dto.CategoryDTO;
import com.team.ordermeal.entity.Merchant;
import com.team.ordermeal.service.DishCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;

@Controller
@RequestMapping("/merchant/category")
public class MerchantCategoryController {

    @Autowired
    private DishCategoryService categoryService;

    @GetMapping
    public String list(HttpSession session, Model model) {
        Merchant merchant = (Merchant) session.getAttribute("merchant");
        if (merchant == null) return "redirect:/merchant/login";
        model.addAttribute("categories", categoryService.listByMerchant(merchant.getId()));
        return "merchant/category_list";
    }

    @PostMapping("/add")
    public String add(CategoryDTO dto, HttpSession session) {
        Merchant merchant = (Merchant) session.getAttribute("merchant");
        if (merchant == null) return "redirect:/merchant/login";
        categoryService.addCategory(merchant.getId(), dto);
        return "redirect:/merchant/category";
    }

    @PostMapping("/update/{id}")
    public String update(@PathVariable Long id, CategoryDTO dto, HttpSession session) {
        Merchant merchant = (Merchant) session.getAttribute("merchant");
        if (merchant == null) return "redirect:/merchant/login";
        categoryService.updateCategory(id, dto);
        return "redirect:/merchant/category";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id, HttpSession session) {
        Merchant merchant = (Merchant) session.getAttribute("merchant");
        if (merchant == null) return "redirect:/merchant/login";
        categoryService.deleteCategory(id, merchant.getId());
        return "redirect:/merchant/category";
    }
}
```

### 7.4 MerchantDishController（菜品管理）
路径：`controller/merchant/MerchantDishController.java`

```java
package com.team.ordermeal.controller.merchant;

import com.team.ordermeal.dto.DishDTO;
import com.team.ordermeal.entity.Merchant;
import com.team.ordermeal.service.DishCategoryService;
import com.team.ordermeal.service.MerchantDishService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;

@Controller
@RequestMapping("/merchant/dish")
public class MerchantDishController {

    @Autowired
    private MerchantDishService dishService;

    @Autowired
    private DishCategoryService categoryService;

    @GetMapping("/list")
    public String list(HttpSession session, Model model) {
        Merchant merchant = (Merchant) session.getAttribute("merchant");
        if (merchant == null) return "redirect:/merchant/login";
        model.addAttribute("dishes", dishService.listByMerchant(merchant.getId()));
        return "merchant/dish_list";
    }

    @GetMapping("/add")
    public String addForm(HttpSession session, Model model) {
        Merchant merchant = (Merchant) session.getAttribute("merchant");
        if (merchant == null) return "redirect:/merchant/login";
        model.addAttribute("categories", categoryService.listByMerchant(merchant.getId()));
        return "merchant/dish_form";
    }

    @PostMapping("/add")
    public String add(DishDTO dto, HttpSession session) {
        Merchant merchant = (Merchant) session.getAttribute("merchant");
        if (merchant == null) return "redirect:/merchant/login";
        dishService.addDish(merchant.getId(), dto);
        return "redirect:/merchant/dish/list";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, HttpSession session, Model model) {
        Merchant merchant = (Merchant) session.getAttribute("merchant");
        if (merchant == null) return "redirect:/merchant/login";
        model.addAttribute("dish", dishService.getDish(id));
        model.addAttribute("categories", categoryService.listByMerchant(merchant.getId()));
        return "merchant/dish_form";
    }

    @PostMapping("/update/{id}")
    public String update(@PathVariable Long id, DishDTO dto, HttpSession session) {
        Merchant merchant = (Merchant) session.getAttribute("merchant");
        if (merchant == null) return "redirect:/merchant/login";
        dto.setId(id);
        dishService.updateDish(id, dto);
        return "redirect:/merchant/dish/list";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id, HttpSession session) {
        Merchant merchant = (Merchant) session.getAttribute("merchant");
        if (merchant == null) return "redirect:/merchant/login";
        dishService.deleteDish(id, merchant.getId());
        return "redirect:/merchant/dish/list";
    }

    @GetMapping("/toggleStatus/{id}")
    public String toggleStatus(@PathVariable Long id, HttpSession session) {
        Merchant merchant = (Merchant) session.getAttribute("merchant");
        if (merchant == null) return "redirect:/merchant/login";
        Dish dish = dishService.getDish(id);
        String newStatus = "ONLINE".equals(dish.getStatus()) ? "OFFLINE" : "ONLINE";
        dishService.updateStatus(id, newStatus, merchant.getId());
        return "redirect:/merchant/dish/list";
    }
}
```

---

## 8. 第六步：前端页面（Thymeleaf + Bootstrap）

所有商家端页面放在 `src/main/resources/templates/merchant/` 下。

### 8.1 商家登录页 `merchant_login.html`

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <title>商家登录</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
</head>
<body class="bg-light">
<div class="container mt-5" style="max-width: 500px;">
    <div class="card shadow">
        <div class="card-header bg-primary text-white">
            <h3>商家登录</h3>
        </div>
        <div class="card-body">
            <div th:if="${param.msg}" class="alert alert-success" th:text="${param.msg}"></div>
            <div th:if="${error}" class="alert alert-danger" th:text="${error}"></div>
            <form method="post">
                <div class="mb-3">
                    <label>用户名</label>
                    <input type="text" name="username" class="form-control" required>
                </div>
                <div class="mb-3">
                    <label>密码</label>
                    <input type="password" name="password" class="form-control" required>
                </div>
                <button type="submit" class="btn btn-primary w-100">登录</button>
            </form>
            <div th:if="${showFindPwd}" class="mt-3 text-center">
                <a href="#">找回密码</a>
            </div>
            <hr>
            <div class="text-center">
                还没有店铺？ <a href="/merchant/register">立即入驻</a>
            </div>
        </div>
    </div>
</div>
</body>
</html>
```

### 8.2 商家注册页 `merchant_register.html`

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <title>商家入驻</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
</head>
<body class="bg-light">
<div class="container mt-5" style="max-width: 600px;">
    <div class="card shadow">
        <div class="card-header bg-success text-white">
            <h3>商家入驻申请</h3>
        </div>
        <div class="card-body">
            <div th:if="${error}" class="alert alert-danger" th:text="${error}"></div>
            <form method="post">
                <div class="mb-2"><label>用户名</label><input type="text" name="username" class="form-control" required></div>
                <div class="mb-2"><label>密码</label><input type="password" name="password" class="form-control" required></div>
                <div class="mb-2"><label>店铺名称</label><input type="text" name="merchantName" class="form-control" required></div>
                <div class="mb-2"><label>法人姓名</label><input type="text" name="legalPerson" class="form-control"></div>
                <div class="mb-2"><label>联系电话</label><input type="tel" name="phone" class="form-control" required></div>
                <div class="mb-2"><label>店铺地址</label><input type="text" name="address" class="form-control"></div>
                <div class="mb-2"><label>营业执照号</label><input type="text" name="businessLicense" class="form-control"></div>
                <button type="submit" class="btn btn-success w-100">提交申请</button>
            </form>
            <hr>
            <div class="text-center">已有账号？ <a href="/merchant/login">登录</a></div>
        </div>
    </div>
</div>
</body>
</html>
```

### 8.3 店铺信息管理 `merchant_info.html`

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <title>店铺信息</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
</head>
<body>
<div class="container mt-4">
    <h2>店铺信息</h2>
    <div th:if="${success}" class="alert alert-success" th:text="${success}"></div>
    <div th:if="${error}" class="alert alert-danger" th:text="${error}"></div>
    <form method="post" action="/merchant/info/update">
        <div class="mb-2">
            <label>店铺名称</label>
            <input type="text" name="merchantName" class="form-control" th:value="${merchant.merchantName}" required>
        </div>
        <div class="mb-2">
            <label>法人</label>
            <input type="text" name="legalPerson" class="form-control" th:value="${merchant.legalPerson}">
        </div>
        <div class="mb-2">
            <label>联系电话</label>
            <input type="tel" name="phone" class="form-control" th:value="${merchant.phone}" required>
        </div>
        <div class="mb-2">
            <label>地址</label>
            <input type="text" name="address" class="form-control" th:value="${merchant.address}">
        </div>
        <div class="mb-2">
            <label>营业执照号</label>
            <input type="text" name="businessLicense" class="form-control" th:value="${merchant.businessLicense}">
        </div>
        <div class="mb-2">
            <label>营业状态</label>
            <select name="businessStatus" class="form-control">
                <option value="OPEN" th:selected="${merchant.businessStatus == 'OPEN'}">营业中</option>
                <option value="CLOSED" th:selected="${merchant.businessStatus == 'CLOSED'}">休息中</option>
            </select>
        </div>
        <button type="submit" class="btn btn-primary">保存修改</button>
        <a href="/merchant/dish/list" class="btn btn-secondary">返回菜品管理</a>
    </form>
</div>
</body>
</html>
```

### 8.4 分类管理 `category_list.html`（简化，显示列表和添加表单）

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <title>菜品分类管理</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
</head>
<body>
<div class="container mt-4">
    <h2>菜品分类</h2>
    <a href="/merchant/dish/list" class="btn btn-secondary mb-3">返回菜品管理</a>
    <div class="row">
        <div class="col-md-6">
            <div class="card">
                <div class="card-header">新增分类</div>
                <div class="card-body">
                    <form method="post" action="/merchant/category/add">
                        <div class="mb-2">
                            <label>分类名称</label>
                            <input type="text" name="categoryName" class="form-control" required>
                        </div>
                        <div class="mb-2">
                            <label>排序</label>
                            <input type="number" name="sortOrder" class="form-control" value="0">
                        </div>
                        <button type="submit" class="btn btn-primary">添加</button>
                    </form>
                </div>
            </div>
        </div>
        <div class="col-md-6">
            <div class="card">
                <div class="card-header">现有分类</div>
                <ul class="list-group list-group-flush">
                    <li th:each="cat : ${categories}" class="list-group-item d-flex justify-content-between align-items-center">
                        <span th:text="${cat.categoryName} + ' (排序:' + ${cat.sortOrder} + ')'"></span>
                        <div>
                            <button class="btn btn-sm btn-warning" data-bs-toggle="collapse" th:data-bs-target="'#edit'+${cat.id}">编辑</button>
                            <a th:href="'/merchant/category/delete/'+${cat.id}" class="btn btn-sm btn-danger" onclick="return confirm('确定删除？')">删除</a>
                        </div>
                        <div th:id="'edit'+${cat.id}" class="collapse w-100 mt-2">
                            <form method="post" th:action="'/merchant/category/update/'+${cat.id}">
                                <input type="text" name="categoryName" th:value="${cat.categoryName}" class="form-control form-control-sm mb-1">
                                <input type="number" name="sortOrder" th:value="${cat.sortOrder}" class="form-control form-control-sm mb-1">
                                <button type="submit" class="btn btn-sm btn-success">保存</button>
                            </form>
                        </div>
                    </li>
                </ul>
            </div>
        </div>
    </div>
</div>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
```

### 8.5 菜品管理列表 `dish_list.html`

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <title>菜品管理</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
</head>
<body>
<div class="container mt-4">
    <h2>我的菜品</h2>
    <div class="mb-3">
        <a href="/merchant/dish/add" class="btn btn-primary">新增菜品</a>
        <a href="/merchant/category" class="btn btn-outline-secondary">分类管理</a>
        <a href="/merchant/info" class="btn btn-outline-info">店铺信息</a>
        <a href="/merchant/logout" class="btn btn-outline-danger float-end">退出登录</a>
    </div>
    <table class="table table-bordered table-hover">
        <thead>
        <tr><th>ID</th><th>菜品名</th><th>分类</th><th>价格</th><th>状态</th><th>销量</th><th>操作</th></tr>
        </thead>
        <tbody>
            <tr th:each="d : ${dishes}">
                <td th:text="${d.id}"></td>
                <td th:text="${d.dishName}"></td>
                <td th:text="${d.categoryId}"></td> <!-- 可优化显示分类名 -->
                <td th:text="'¥'+${d.price}"></td>
                <td>
                    <span th:if="${d.status == 'ONLINE'}" class="badge bg-success">上架</span>
                    <span th:if="${d.status == 'OFFLINE'}" class="badge bg-secondary">下架</span>
                </td>
                <td th:text="${d.sales}"></td>
                <td>
                    <a th:href="'/merchant/dish/edit/'+${d.id}" class="btn btn-sm btn-warning">编辑</a>
                    <a th:href="'/merchant/dish/toggleStatus/'+${d.id}" class="btn btn-sm btn-info" th:text="${d.status == 'ONLINE' ? '下架' : '上架'}"></a>
                    <a th:href="'/merchant/dish/delete/'+${d.id}" class="btn btn-sm btn-danger" onclick="return confirm('确定删除？')">删除</a>
                </td>
            </tr>
        </tbody>
    </table>
</div>
</body>
</html>
```

### 8.6 菜品表单 `dish_form.html`（新增/编辑共用）

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <title>编辑菜品</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
</head>
<body>
<div class="container mt-4" style="max-width: 600px;">
    <h2 th:if="${dish == null}" th:text="新增菜品"></h2>
    <h2 th:if="${dish != null}" th:text="编辑菜品"></h2>
    <form th:action="${dish == null ? '/merchant/dish/add' : '/merchant/dish/update/'+dish.id}" method="post">
        <div class="mb-2">
            <label>菜品名称</label>
            <input type="text" name="dishName" class="form-control" th:value="${dish != null ? dish.dishName : ''}" required>
        </div>
        <div class="mb-2">
            <label>分类</label>
            <select name="categoryId" class="form-control">
                <option value="">无分类</option>
                <option th:each="cat : ${categories}" th:value="${cat.id}" th:text="${cat.categoryName}" th:selected="${dish != null && dish.categoryId == cat.id}"></option>
            </select>
        </div>
        <div class="mb-2">
            <label>价格</label>
            <input type="number" step="0.01" name="price" class="form-control" th:value="${dish != null ? dish.price : ''}" required>
        </div>
        <div class="mb-2">
            <label>原价</label>
            <input type="number" step="0.01" name="originalPrice" class="form-control" th:value="${dish != null ? dish.originalPrice : ''}">
        </div>
        <div class="mb-2">
            <label>图片路径</label>
            <input type="text" name="image" class="form-control" th:value="${dish != null ? dish.image : ''}" placeholder="/images/xxx.jpg">
        </div>
        <div class="mb-2">
            <label>描述</label>
            <textarea name="description" class="form-control" rows="3" th:text="${dish != null ? dish.description : ''}"></textarea>
        </div>
        <div class="mb-2">
            <label>状态</label>
            <select name="status" class="form-control">
                <option value="ONLINE" th:selected="${dish == null || dish.status == 'ONLINE'}">上架</option>
                <option value="OFFLINE" th:selected="${dish != null && dish.status == 'OFFLINE'}">下架</option>
            </select>
        </div>
        <button type="submit" class="btn btn-primary">保存</button>
        <a href="/merchant/dish/list" class="btn btn-secondary">取消</a>
    </form>
</div>
</body>
</html>
```

---

## 9. 第七步：测试与调试

### 9.1 测试商家注册与登录
1. 访问 `http://localhost:8080/merchant/register`，填写表单提交。
2. 数据库 `tb_merchant` 应新增一条 `PENDING` 记录。
3. 管理员（组长）审批通过后（修改 status 为 'APPROVED'），才能登录。
4. 登录后应跳转到菜品管理页。

### 9.2 测试店铺信息修改
- 登录后进入 `/merchant/info`，修改信息并保存，检查数据库是否更新。

### 9.3 测试分类管理
- 添加分类，列表显示，编辑删除功能正常。

### 9.4 测试菜品管理
- 新增菜品，上传图片路径（可暂时不传真实图片）。
- 编辑菜品，修改价格，上下架。
- 删除菜品。

### 9.5 与组员B的协作验证
- 组员B的菜品浏览页应该能看到你添加的且状态为 `ONLINE` 的菜品。如果组员B尚未完成，你可以暂时忽略。

---

## 10. 第八步：提交代码到 Git

```bash
git add .
git commit -m "feat(商家): 完成商家注册登录、店铺信息、分类与菜品管理"
git push origin feature/merchant
```

创建 Pull Request 合并到 `develop`。

---

## 11. 常见问题与解决方案

### Q1: 商家注册后登录提示“账号未审核通过”
- 正常业务逻辑。你需要联系组长（管理员）在后台审批。测试时可以直接修改数据库状态为 `APPROVED`。

### Q2: 菜品管理页面分类下拉框不显示分类
- 确认分类表中是否有当前商家的分类，并且你在 Controller 中传入了 `categories` 属性。

### Q3: 删除分类时报外键约束错误
- 因为菜品表中 `category_id` 引用了分类ID。你应该在删除分类前检查该分类下是否有菜品，或者选择级联删除。为了简化，可以在前端提示“请先删除或转移该分类下的菜品”。

### Q4: 图片上传功能未实现
- 目前只需要填写图片路径字符串，真实图片上传可后续扩展（使用 Spring 文件上传）。演示时可以直接用网络图片 URL。

### Q5: 与组员B的 Mapper 方法冲突
- 组长在合并时会解决冲突。你在开发时确保 `DishMapper` 中包含了 `insert`、`updateById`、`deleteById` 等方法即可。如果组员B没有这些方法，你会遇到编译错误，可以临时在本地添加，合并时告知组长。

---

**下一步**：你的商家端完成后，组员D（订单创建）和组员E（商家订单管理）将依赖你的商家数据和菜品数据。请及时通知组长合并你的分支，以便其他组员能基于最新的 `develop` 分支继续开发。祝你顺利！