# 在线点餐系统 - 组员A 指导手册



> 适用角色：组员A  
> 核心职责：用户端功能（登录、注册、个人中心、地址管理、我的订单）  
> **前置依赖**：组长已完成项目脚手架、数据库设计、公共类（Result、异常处理、密码加密）  
> **协作提示**：需要调用组长定义的订单状态枚举，并调用组员D/E提供的订单查询接口（可先 mock）

---

## 📌 目录

1. [你的任务范围](#1-你的任务范围)
2. [开始前的准备](#2-开始前的准备)
3. [第一步：从 Git 拉取组长的基础代码](#3-第一步从-git-拉取组长的基础代码)
4. [第二步：创建你自己的包和类](#4-第二步创建你自己的包和类)
5. [第三步：实体类（User, Address）](#5-第三步实体类user-address)
6. [第四步：Mapper 层（数据库操作）](#6-第四步mapper-层数据库操作)
7. [第五步：Service 层（业务逻辑）](#7-第五步service-层业务逻辑)
8. [第六步：Controller 层（处理 HTTP 请求）](#8-第六步controller-层处理-http-请求)
9. [第七步：前端页面（Thymeleaf + Bootstrap）](#9-第七步前端页面thymeleaf--bootstrap)
10. [第八步：测试与调试](#10-第八步测试与调试)
11. [第九步：提交代码到 Git](#11-第九步提交代码到-git)
12. [常见问题与解决方案](#12-常见问题与解决方案)

---

## 1. 你的任务范围

| 功能模块 | 说明                                                         | 是否独立                           |
| -------- | ------------------------------------------------------------ | ---------------------------------- |
| 用户注册 | 用户名、密码、手机号（必填），密码加密存储                   | ✅ 独立                             |
| 用户登录 | 用户名/手机号+密码，错误时跳转注册或显示找回密码按钮         | ✅ 独立                             |
| 个人中心 | 修改密码、修改手机号（模拟短信验证）                         | ✅ 独立                             |
| 地址管理 | 新增、编辑、删除、设置默认收货地址                           | ✅ 独立                             |
| 我的订单 | 查询当前用户的订单列表，按状态筛选（待支付、已支付、进行中、已完成、已取消） | ⚠️ 依赖组员D/E的订单数据（先 mock） |

**你需要实现的前端页面**：
- `login.html` - 登录页
- `register.html` - 注册页
- `profile.html` - 个人中心（修改密码、修改手机号）
- `address_list.html` - 地址管理列表
- `address_form.html` - 添加/编辑地址的表单
- `my_orders.html` - 我的订单列表

---

## 2. 开始前的准备

### 2.1 确保组长已完成以下工作
- [x] SpringBoot 项目创建，Maven 依赖正确
- [x] `application.properties` 配置好数据库连接
- [x] 数据库表 `tb_user`、`tb_address`、`tb_order` 已创建（含测试数据）
- [x] 公共类 `Result`、`BusinessException`、`GlobalExceptionHandler` 已存在
- [x] 密码加密工具 `PasswordEncoderUtil` 已存在

### 2.2 你的电脑环境
- JDK 17、Maven、MySQL、Git、IDEA 已安装（参考组长手册第1节）

---

## 3. 第一步：从 Git 拉取组长的基础代码

> 组长会先创建 Gitee 仓库并推送初始代码。你需要克隆到本地。

### 3.1 克隆仓库（只需要做一次）
打开命令行（或 IDEA 的 Terminal），执行：
```bash
git clone https://gitee.com/你的组名/online-ordermeal.git
cd online-ordermeal
```

### 3.2 切换到 develop 分支
```bash
git checkout develop
```

### 3.3 创建你自己的功能分支（所有开发都在这个分支上）
```bash
git checkout -b feature/user-module
```

### 3.4 用 IDEA 打开项目
- 点击 `File` → `Open` → 选择刚才克隆的文件夹
- 等待 Maven 下载依赖（右下角进度条）

### 3.5 验证项目能启动
找到 `OrmeaApplication.java`（在 `com.team.ordermeal` 包下），右键 → `Run 'OrmeaApplication'`。  
控制台输出 `Started OrmeaApplication in xxx seconds` 表示成功。  
浏览器访问 `http://localhost:8080`（虽然还没页面，但不报错即可）。

---

## 4. 第二步：创建你自己的包和类

在 `com.team.ordermeal` 下，你需要创建或使用以下包（组长已创建了部分，检查是否存在）：

```
com.team.ordermeal
├── controller
│   └── AuthController.java      （登录注册）
│   └── UserController.java      （个人中心、地址、我的订单）
├── service
│   ├── UserService.java
│   └── impl
│       └── UserServiceImpl.java
├── mapper
│   ├── UserMapper.java
│   └── AddressMapper.java
├── entity
│   ├── User.java
│   └── Address.java
└── dto
    ├── LoginDTO.java
    ├── RegisterDTO.java
    ├── PasswordUpdateDTO.java
    └── AddressDTO.java
```

**操作步骤**：
- 在 `controller` 包上右键 → New → Java Class → 输入 `AuthController`
- 同样创建 `UserController`
- 其他包类似。如果包不存在，先创建包（右键 → New → Package）

---

## 5. 第三步：实体类（User, Address）

### 5.1 用户实体 `User.java`
路径：`entity/User.java`

```java
package com.team.ordermeal.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 用户实体，对应数据库表 tb_user
 */
@Data  // Lombok 自动生成 getter/setter/toString/equals/hashCode
public class User {
    private Long id;                // 用户ID，自增主键
    private String username;        // 登录用户名，唯一
    private String password;        // 加密后的密码
    private String phone;           // 手机号
    private String status;          // ACTIVE / BANNED
    private String role;            // USER / ADMIN（如果组长加了该字段）
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
```

> ⚠️ 注意：如果组长建的 `tb_user` 表没有 `role` 字段，你可以不加这个属性，或者让组长执行 `ALTER TABLE tb_user ADD COLUMN role VARCHAR(20) DEFAULT 'USER';`

### 5.2 地址实体 `Address.java`
路径：`entity/Address.java`

```java
package com.team.ordermeal.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 收货地址，对应表 tb_address
 */
@Data
public class Address {
    private Long id;
    private Long userId;            // 所属用户ID
    private String receiverName;    // 收货人姓名
    private String phone;           // 收货人电话
    private String province;        // 省
    private String city;            // 市
    private String district;        // 区/县
    private String detailAddress;   // 详细地址
    private Integer isDefault;      // 1=默认地址，0=非默认
    private LocalDateTime createTime;
}
```

---

## 6. 第四步：Mapper 层（数据库操作）

### 6.1 UserMapper 接口
路径：`mapper/UserMapper.java`

```java
package com.team.ordermeal.mapper;

import com.team.ordermeal.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserMapper {

    /**
     * 根据用户名查询用户（登录用）
     */
    User selectByUsername(@Param("username") String username);

    /**
     * 根据手机号查询用户（注册时检查手机号是否已存在）
     */
    User selectByPhone(@Param("phone") String phone);

    /**
     * 插入新用户
     */
    int insert(User user);

    /**
     * 更新用户信息（修改密码、手机号等）
     */
    int updateById(User user);

    /**
     * 根据ID查询用户
     */
    User selectById(@Param("id") Long id);
}
```

### 6.2 对应的 XML 映射文件
在 `src/main/resources/mapper/` 下创建 `UserMapper.xml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.team.ordermeal.mapper.UserMapper">

    <!-- 结果映射：将数据库字段与Java属性对应 -->
    <resultMap id="BaseResultMap" type="com.team.ordermeal.entity.User">
        <id column="id" property="id"/>
        <result column="username" property="username"/>
        <result column="password" property="password"/>
        <result column="phone" property="phone"/>
        <result column="status" property="status"/>
        <result column="role" property="role"/>
        <result column="create_time" property="createTime"/>
        <result column="update_time" property="updateTime"/>
    </resultMap>

    <!-- 根据用户名查询 -->
    <select id="selectByUsername" resultMap="BaseResultMap">
        SELECT * FROM tb_user WHERE username = #{username}
    </select>

    <!-- 根据手机号查询 -->
    <select id="selectByPhone" resultMap="BaseResultMap">
        SELECT * FROM tb_user WHERE phone = #{phone}
    </select>

    <!-- 插入用户（注意：id 是自增的，不需要手动设置） -->
    <insert id="insert" useGeneratedKeys="true" keyProperty="id">
        INSERT INTO tb_user (username, password, phone, status, role)
        VALUES (#{username}, #{password}, #{phone}, 'ACTIVE', 'USER')
    </insert>

    <!-- 更新用户 -->
    <update id="updateById">
        UPDATE tb_user
        SET password = #{password},
            phone = #{phone},
            update_time = NOW()
        WHERE id = #{id}
    </update>

    <!-- 根据ID查询 -->
    <select id="selectById" resultMap="BaseResultMap">
        SELECT * FROM tb_user WHERE id = #{id}
    </select>

</mapper>
```

### 6.3 AddressMapper 接口
路径：`mapper/AddressMapper.java`

```java
package com.team.ordermeal.mapper;

import com.team.ordermeal.entity.Address;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface AddressMapper {

    /**
     * 查询某个用户的所有地址
     */
    List<Address> selectByUserId(@Param("userId") Long userId);

    /**
     * 查询某个用户的默认地址
     */
    Address selectDefaultByUserId(@Param("userId") Long userId);

    /**
     * 根据ID查询地址
     */
    Address selectById(@Param("id") Long id);

    /**
     * 插入新地址
     */
    int insert(Address address);

    /**
     * 更新地址
     */
    int updateById(Address address);

    /**
     * 删除地址
     */
    int deleteById(@Param("id") Long id);

    /**
     * 将该用户所有地址的 is_default 设为 0
     */
    int clearDefaultByUserId(@Param("userId") Long userId);
}
```

### 6.4 AddressMapper.xml
在 `src/main/resources/mapper/` 下创建 `AddressMapper.xml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.team.ordermeal.mapper.AddressMapper">

    <resultMap id="BaseResultMap" type="com.team.ordermeal.entity.Address">
        <id column="id" property="id"/>
        <result column="user_id" property="userId"/>
        <result column="receiver_name" property="receiverName"/>
        <result column="phone" property="phone"/>
        <result column="province" property="province"/>
        <result column="city" property="city"/>
        <result column="district" property="district"/>
        <result column="detail_address" property="detailAddress"/>
        <result column="is_default" property="isDefault"/>
        <result column="create_time" property="createTime"/>
    </resultMap>

    <select id="selectByUserId" resultMap="BaseResultMap">
        SELECT * FROM tb_address WHERE user_id = #{userId} ORDER BY is_default DESC, create_time DESC
    </select>

    <select id="selectDefaultByUserId" resultMap="BaseResultMap">
        SELECT * FROM tb_address WHERE user_id = #{userId} AND is_default = 1 LIMIT 1
    </select>

    <select id="selectById" resultMap="BaseResultMap">
        SELECT * FROM tb_address WHERE id = #{id}
    </select>

    <insert id="insert" useGeneratedKeys="true" keyProperty="id">
        INSERT INTO tb_address (user_id, receiver_name, phone, province, city, district, detail_address, is_default)
        VALUES (#{userId}, #{receiverName}, #{phone}, #{province}, #{city}, #{district}, #{detailAddress}, #{isDefault})
    </insert>

    <update id="updateById">
        UPDATE tb_address
        SET receiver_name = #{receiverName},
            phone = #{phone},
            province = #{province},
            city = #{city},
            district = #{district},
            detail_address = #{detailAddress},
            is_default = #{isDefault}
        WHERE id = #{id}
    </update>

    <delete id="deleteById">
        DELETE FROM tb_address WHERE id = #{id}
    </delete>

    <update id="clearDefaultByUserId">
        UPDATE tb_address SET is_default = 0 WHERE user_id = #{userId}
    </update>

</mapper>
```

---

## 7. 第五步：Service 层（业务逻辑）

### 7.1 UserService 接口
路径：`service/UserService.java`

```java
package com.team.ordermeal.service;

import com.team.ordermeal.entity.User;
import com.team.ordermeal.dto.RegisterDTO;
import com.team.ordermeal.dto.PasswordUpdateDTO;

public interface UserService {

    /**
     * 用户注册
     * @return 注册成功的用户对象
     */
    User register(RegisterDTO dto);

    /**
     * 用户登录验证
     * @param loginName 用户名或手机号
     * @param rawPassword 明文密码
     * @return 登录成功的用户对象，失败返回 null（由 Controller 处理具体错误）
     */
    User login(String loginName, String rawPassword);

    /**
     * 修改密码
     */
    void updatePassword(Long userId, String oldPassword, String newPassword);

    /**
     * 修改手机号（简单模拟，不涉及真实短信）
     */
    void updatePhone(Long userId, String newPhone);

    /**
     * 根据ID获取用户
     */
    User getUserById(Long userId);
}
```

### 7.2 UserServiceImpl 实现类
路径：`service/impl/UserServiceImpl.java`

```java
package com.team.ordermeal.service.impl;

import com.team.ordermeal.common.exception.BusinessException;
import com.team.ordermeal.common.utils.PasswordEncoderUtil;
import com.team.ordermeal.dto.RegisterDTO;
import com.team.ordermeal.dto.PasswordUpdateDTO;
import com.team.ordermeal.entity.User;
import com.team.ordermeal.mapper.UserMapper;
import com.team.ordermeal.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Override
    @Transactional
    public User register(RegisterDTO dto) {
        // 1. 检查用户名是否已存在
        if (userMapper.selectByUsername(dto.getUsername()) != null) {
            throw new BusinessException("用户名已存在");
        }
        // 2. 检查手机号是否已存在（可选，根据需求）
        if (userMapper.selectByPhone(dto.getPhone()) != null) {
            throw new BusinessException("手机号已被注册");
        }
        // 3. 创建用户对象
        User user = new User();
        user.setUsername(dto.getUsername());
        // 密码加密
        user.setPassword(PasswordEncoderUtil.encode(dto.getPassword()));
        user.setPhone(dto.getPhone());
        user.setStatus("ACTIVE");
        user.setRole("USER");
        // 4. 插入数据库
        userMapper.insert(user);
        return user;
    }

    @Override
    public User login(String loginName, String rawPassword) {
        // 1. 先按用户名查询
        User user = userMapper.selectByUsername(loginName);
        // 2. 如果查不到，再按手机号查询
        if (user == null) {
            user = userMapper.selectByPhone(loginName);
        }
        // 3. 用户不存在
        if (user == null) {
            return null;  // 让 Controller 决定跳转到注册页
        }
        // 4. 密码校验
        if (!PasswordEncoderUtil.matches(rawPassword, user.getPassword())) {
            throw new BusinessException("密码错误");
        }
        // 5. 检查账号状态
        if (!"ACTIVE".equals(user.getStatus())) {
            throw new BusinessException("账号已被封禁");
        }
        return user;
    }

    @Override
    public void updatePassword(Long userId, String oldPassword, String newPassword) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        if (!PasswordEncoderUtil.matches(oldPassword, user.getPassword())) {
            throw new BusinessException("原密码错误");
        }
        user.setPassword(PasswordEncoderUtil.encode(newPassword));
        userMapper.updateById(user);
    }

    @Override
    public void updatePhone(Long userId, String newPhone) {
        // 检查新手机号是否已被其他用户占用
        User exist = userMapper.selectByPhone(newPhone);
        if (exist != null && !exist.getId().equals(userId)) {
            throw new BusinessException("手机号已被其他用户绑定");
        }
        User user = userMapper.selectById(userId);
        user.setPhone(newPhone);
        userMapper.updateById(user);
    }

    @Override
    public User getUserById(Long userId) {
        return userMapper.selectById(userId);
    }
}
```

### 7.3 AddressService 接口（地址管理）
路径：`service/AddressService.java`

```java
package com.team.ordermeal.service;

import com.team.ordermeal.entity.Address;
import com.team.ordermeal.dto.AddressDTO;
import java.util.List;

public interface AddressService {
    List<Address> listByUser(Long userId);
    Address getDefaultAddress(Long userId);
    void addAddress(Long userId, AddressDTO dto);
    void updateAddress(Long addressId, AddressDTO dto);
    void deleteAddress(Long addressId, Long userId);
    void setDefault(Long addressId, Long userId);
}
```

### 7.4 AddressServiceImpl 实现类
路径：`service/impl/AddressServiceImpl.java`

```java
package com.team.ordermeal.service.impl;

import com.team.ordermeal.common.exception.BusinessException;
import com.team.ordermeal.dto.AddressDTO;
import com.team.ordermeal.entity.Address;
import com.team.ordermeal.mapper.AddressMapper;
import com.team.ordermeal.service.AddressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class AddressServiceImpl implements AddressService {

    @Autowired
    private AddressMapper addressMapper;

    @Override
    public List<Address> listByUser(Long userId) {
        return addressMapper.selectByUserId(userId);
    }

    @Override
    public Address getDefaultAddress(Long userId) {
        return addressMapper.selectDefaultByUserId(userId);
    }

    @Override
    @Transactional
    public void addAddress(Long userId, AddressDTO dto) {
        Address address = new Address();
        address.setUserId(userId);
        address.setReceiverName(dto.getReceiverName());
        address.setPhone(dto.getPhone());
        address.setProvince(dto.getProvince());
        address.setCity(dto.getCity());
        address.setDistrict(dto.getDistrict());
        address.setDetailAddress(dto.getDetailAddress());
        // 如果设置为默认地址，需要先清除该用户的其他默认地址
        if (dto.getIsDefault() == 1) {
            addressMapper.clearDefaultByUserId(userId);
            address.setIsDefault(1);
        } else {
            // 如果用户还没有任何地址，自动将第一个设为默认
            if (addressMapper.selectByUserId(userId).isEmpty()) {
                address.setIsDefault(1);
            } else {
                address.setIsDefault(0);
            }
        }
        addressMapper.insert(address);
    }

    @Override
    @Transactional
    public void updateAddress(Long addressId, AddressDTO dto) {
        Address address = addressMapper.selectById(addressId);
        if (address == null) {
            throw new BusinessException("地址不存在");
        }
        address.setReceiverName(dto.getReceiverName());
        address.setPhone(dto.getPhone());
        address.setProvince(dto.getProvince());
        address.setCity(dto.getCity());
        address.setDistrict(dto.getDistrict());
        address.setDetailAddress(dto.getDetailAddress());
        if (dto.getIsDefault() == 1) {
            addressMapper.clearDefaultByUserId(address.getUserId());
            address.setIsDefault(1);
        } else {
            address.setIsDefault(0);
        }
        addressMapper.updateById(address);
    }

    @Override
    public void deleteAddress(Long addressId, Long userId) {
        Address address = addressMapper.selectById(addressId);
        if (address == null || !address.getUserId().equals(userId)) {
            throw new BusinessException("地址不存在或无权限");
        }
        addressMapper.deleteById(addressId);
    }

    @Override
    @Transactional
    public void setDefault(Long addressId, Long userId) {
        // 先清除该用户所有默认地址
        addressMapper.clearDefaultByUserId(userId);
        // 再将指定地址设为默认
        Address address = addressMapper.selectById(addressId);
        if (address == null || !address.getUserId().equals(userId)) {
            throw new BusinessException("地址不存在");
        }
        address.setIsDefault(1);
        addressMapper.updateById(address);
    }
}
```

---

## 8. 第六步：Controller 层（处理 HTTP 请求）

### 8.1 数据传输对象 DTO
路径：`dto/RegisterDTO.java`

```java
package com.team.ordermeal.dto;

import lombok.Data;

@Data
public class RegisterDTO {
    private String username;
    private String password;
    private String phone;
}
```

`dto/PasswordUpdateDTO.java`
```java
package com.team.ordermeal.dto;

import lombok.Data;

@Data
public class PasswordUpdateDTO {
    private String oldPassword;
    private String newPassword;
}
```

`dto/AddressDTO.java`
```java
package com.team.ordermeal.dto;

import lombok.Data;

@Data
public class AddressDTO {
    private String receiverName;
    private String phone;
    private String province;
    private String city;
    private String district;
    private String detailAddress;
    private Integer isDefault;  // 0或1
}
```

### 8.2 AuthController（登录注册）
路径：`controller/AuthController.java`

```java
package com.team.ordermeal.controller;

import com.team.ordermeal.common.result.Result;
import com.team.ordermeal.dto.RegisterDTO;
import com.team.ordermeal.entity.User;
import com.team.ordermeal.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;

@Controller
public class AuthController {

    @Autowired
    private UserService userService;

    // 显示登录页
    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    // 处理登录请求（表单提交）
    @PostMapping("/login")
    public String login(@RequestParam String loginName,
                        @RequestParam String password,
                        HttpSession session,
                        Model model) {
        try {
            User user = userService.login(loginName, password);
            if (user == null) {
                // 用户名不存在，跳转到注册页
                return "redirect:/register";
            }
            session.setAttribute("user", user);
            return "redirect:/dish/list";  // 跳转到菜品浏览页（组员B负责）
        } catch (Exception e) {
            // 密码错误或其他异常
            model.addAttribute("error", e.getMessage());
            model.addAttribute("showFindPwd", true);  // 显示找回密码按钮
            return "login";
        }
    }

    // 显示注册页
    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }

    // 处理注册请求
    @PostMapping("/register")
    public String register(RegisterDTO dto, Model model) {
        try {
            userService.register(dto);
            return "redirect:/login";  // 注册成功跳转到登录页
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "register";
        }
    }

    // 退出登录
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}
```

### 8.3 UserController（个人中心、地址、我的订单）
路径：`controller/UserController.java`

```java
package com.team.ordermeal.controller;

import com.team.ordermeal.common.result.Result;
import com.team.ordermeal.dto.AddressDTO;
import com.team.ordermeal.dto.PasswordUpdateDTO;
import com.team.ordermeal.entity.Address;
import com.team.ordermeal.entity.User;
import com.team.ordermeal.service.AddressService;
import com.team.ordermeal.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;

@Controller
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private AddressService addressService;

    // 个人中心首页（显示用户信息，修改密码/手机号的入口）
    @GetMapping("/profile")
    public String profile(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login";
        model.addAttribute("user", user);
        return "profile";
    }

    // 修改密码（表单提交）
    @PostMapping("/updatePassword")
    public String updatePassword(PasswordUpdateDTO dto, HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        try {
            userService.updatePassword(user.getId(), dto.getOldPassword(), dto.getNewPassword());
            model.addAttribute("success", "密码修改成功，请重新登录");
            session.invalidate();  // 强制重新登录
            return "redirect:/login";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "profile";
        }
    }

    // 修改手机号（简单模拟，无需验证码）
    @PostMapping("/updatePhone")
    public String updatePhone(@RequestParam String newPhone, HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        try {
            userService.updatePhone(user.getId(), newPhone);
            user.setPhone(newPhone);  // 更新 session 中的信息
            session.setAttribute("user", user);
            model.addAttribute("success", "手机号修改成功");
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
        }
        return "profile";
    }

    // ========== 地址管理 ==========
    @GetMapping("/address")
    public String addressList(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        model.addAttribute("addressList", addressService.listByUser(user.getId()));
        return "address_list";
    }

    @GetMapping("/address/add")
    public String addAddressForm() {
        return "address_form";
    }

    @PostMapping("/address/add")
    public String addAddress(AddressDTO dto, HttpSession session) {
        User user = (User) session.getAttribute("user");
        addressService.addAddress(user.getId(), dto);
        return "redirect:/user/address";
    }

    @GetMapping("/address/edit/{id}")
    public String editAddressForm(@PathVariable Long id, HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        // 验证权限：只能编辑自己的地址
        Address address = addressService.listByUser(user.getId()).stream()
                .filter(a -> a.getId().equals(id))
                .findFirst()
                .orElse(null);
        if (address == null) return "redirect:/user/address";
        model.addAttribute("address", address);
        return "address_form";
    }

    @PostMapping("/address/update/{id}")
    public String updateAddress(@PathVariable Long id, AddressDTO dto, HttpSession session) {
        User user = (User) session.getAttribute("user");
        addressService.updateAddress(id, dto);
        return "redirect:/user/address";
    }

    @GetMapping("/address/delete/{id}")
    public String deleteAddress(@PathVariable Long id, HttpSession session) {
        User user = (User) session.getAttribute("user");
        addressService.deleteAddress(id, user.getId());
        return "redirect:/user/address";
    }

    @GetMapping("/address/setDefault/{id}")
    public String setDefaultAddress(@PathVariable Long id, HttpSession session) {
        User user = (User) session.getAttribute("user");
        addressService.setDefault(id, user.getId());
        return "redirect:/user/address";
    }

    // ========== 我的订单（需要调用组员D/E的订单服务） ==========
    // 注意：这里依赖 OrderService，需要等组员D/E完成后才能正常工作。
    // 在开发初期，你可以先写一个假的 OrderService 返回空列表，或者让组长提供一个接口。
    @GetMapping("/orders")
    public String myOrders(@RequestParam(required = false) String status,
                           HttpSession session,
                           Model model) {
        User user = (User) session.getAttribute("user");
        // TODO: 等组员D/E提供 OrderService 后，调用 service.listByUser(user.getId(), status)
        // 现在先 mock 一个空列表，让页面能显示“暂无订单”
        model.addAttribute("orders", java.util.Collections.emptyList());
        model.addAttribute("currentStatus", status);
        return "my_orders";
    }
}
```

---

## 9. 第七步：前端页面（Thymeleaf + Bootstrap）

所有页面放在 `src/main/resources/templates/` 下。  
使用 Bootstrap 5 实现响应式布局。

### 9.1 公共布局（可选）
为了复用头部和尾部，可以创建 `fragments.html`，但为了简洁，每个页面可独立写。

### 9.2 登录页 `login.html`

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>用户登录 - 在线点餐系统</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
</head>
<body class="bg-light">
<div class="container mt-5" style="max-width: 500px;">
    <div class="card shadow">
        <div class="card-header bg-primary text-white">
            <h3 class="mb-0">用户登录</h3>
        </div>
        <div class="card-body">
            <!-- 显示错误信息 -->
            <div th:if="${error}" class="alert alert-danger" th:text="${error}"></div>
            
            <form method="post" action="/login">
                <div class="mb-3">
                    <label class="form-label">用户名/手机号</label>
                    <input type="text" name="loginName" class="form-control" required>
                </div>
                <div class="mb-3">
                    <label class="form-label">密码</label>
                    <input type="password" name="password" class="form-control" required>
                </div>
                <button type="submit" class="btn btn-primary w-100">登录</button>
            </form>
            
            <!-- 密码错误时显示找回密码按钮 -->
            <div th:if="${showFindPwd}" class="mt-3 text-center">
                <a href="#" class="btn btn-link">找回密码</a>
            </div>
            <hr>
            <div class="text-center">
                还没有账号？ <a href="/register">立即注册</a>
            </div>
        </div>
    </div>
</div>
</body>
</html>
```

### 9.3 注册页 `register.html`

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>用户注册</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
</head>
<body class="bg-light">
<div class="container mt-5" style="max-width: 500px;">
    <div class="card shadow">
        <div class="card-header bg-success text-white">
            <h3 class="mb-0">用户注册</h3>
        </div>
        <div class="card-body">
            <div th:if="${error}" class="alert alert-danger" th:text="${error}"></div>
            <form method="post" action="/register">
                <div class="mb-3">
                    <label class="form-label">用户名</label>
                    <input type="text" name="username" class="form-control" required>
                </div>
                <div class="mb-3">
                    <label class="form-label">密码</label>
                    <input type="password" name="password" class="form-control" required>
                </div>
                <div class="mb-3">
                    <label class="form-label">手机号</label>
                    <input type="tel" name="phone" class="form-control" required>
                    <small class="text-muted">用于订单号生成</small>
                </div>
                <button type="submit" class="btn btn-success w-100">注册</button>
            </form>
            <hr>
            <div class="text-center">
                已有账号？ <a href="/login">去登录</a>
            </div>
        </div>
    </div>
</div>
</body>
</html>
```

### 9.4 个人中心 `profile.html`（只展示关键部分）

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <title>个人中心</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
</head>
<body>
<div class="container mt-4">
    <h2>个人中心</h2>
    <div th:if="${success}" class="alert alert-success" th:text="${success}"></div>
    <div th:if="${error}" class="alert alert-danger" th:text="${error}"></div>
    
    <div class="row">
        <div class="col-md-6">
            <div class="card mb-3">
                <div class="card-header">修改密码</div>
                <div class="card-body">
                    <form method="post" action="/user/updatePassword">
                        <div class="mb-2">
                            <label>原密码</label>
                            <input type="password" name="oldPassword" class="form-control" required>
                        </div>
                        <div class="mb-2">
                            <label>新密码</label>
                            <input type="password" name="newPassword" class="form-control" required>
                        </div>
                        <button type="submit" class="btn btn-primary">修改密码</button>
                    </form>
                </div>
            </div>
        </div>
        <div class="col-md-6">
            <div class="card mb-3">
                <div class="card-header">修改手机号</div>
                <div class="card-body">
                    <p>当前手机号：<span th:text="${user.phone}"></span></p>
                    <form method="post" action="/user/updatePhone">
                        <div class="mb-2">
                            <label>新手机号</label>
                            <input type="tel" name="newPhone" class="form-control" required>
                        </div>
                        <button type="submit" class="btn btn-info">更新手机号</button>
                    </form>
                </div>
            </div>
        </div>
    </div>
    
    <div class="mt-3">
        <a href="/user/address" class="btn btn-outline-secondary">地址管理</a>
        <a href="/user/orders" class="btn btn-outline-secondary">我的订单</a>
        <a href="/logout" class="btn btn-outline-danger">退出登录</a>
    </div>
</div>
</body>
</html>
```

### 9.5 地址列表 `address_list.html`（核心）

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <title>地址管理</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
</head>
<body>
<div class="container mt-4">
    <h2>我的地址</h2>
    <a href="/user/address/add" class="btn btn-primary mb-3">新增地址</a>
    <div class="row">
        <div class="col-md-6" th:each="addr : ${addressList}">
            <div class="card mb-2">
                <div class="card-body">
                    <h5 class="card-title" th:text="${addr.receiverName} + ' (' + ${addr.phone} + ')'"></h5>
                    <p class="card-text" th:text="${addr.province}+' '+${addr.city}+' '+${addr.district}+' '+${addr.detailAddress}"></p>
                    <span th:if="${addr.isDefault == 1}" class="badge bg-success">默认地址</span>
                    <div class="mt-2">
                        <a th:href="'/user/address/edit/'+${addr.id}" class="btn btn-sm btn-warning">编辑</a>
                        <a th:href="'/user/address/delete/'+${addr.id}" class="btn btn-sm btn-danger" onclick="return confirm('确定删除？')">删除</a>
                        <a th:unless="${addr.isDefault == 1}" th:href="'/user/address/setDefault/'+${addr.id}" class="btn btn-sm btn-secondary">设为默认</a>
                    </div>
                </div>
            </div>
        </div>
    </div>
    <a href="/user/profile" class="btn btn-link">返回个人中心</a>
</div>
</body>
</html>
```

### 9.6 地址表单 `address_form.html`（省略，与常规表单类似）

`my_orders.html` 暂时留空，因为依赖组员D/E的数据，可以简单写：
```html
<h2>我的订单</h2>
<p>暂未开发（等待订单模块完成）</p>
```

---

## 10. 第八步：测试与调试

### 10.1 单元测试（可选）
可以写简单的 Controller 测试，但不强制。建议手动测试以下场景：

| 测试项                 | 预期结果                               |
| ---------------------- | -------------------------------------- |
| 注册新用户             | 成功后跳转登录页，数据库多一条记录     |
| 登录（正确用户名密码） | 跳转到 `/dish/list`，session 中有 user |
| 登录（用户名不存在）   | 跳转到 `/register`                     |
| 登录（密码错误）       | 页面显示“密码错误”，出现“找回密码”按钮 |
| 修改密码               | 成功后 session 失效，需重新登录        |
| 修改手机号             | 个人中心显示新手机号                   |
| 地址增删改查           | 列表正确显示，默认地址逻辑正确         |

### 10.2 运行项目
```bash
mvn spring-boot:run
```
访问 `http://localhost:8080/login`。

---

## 11. 第九步：提交代码到 Git

```bash
# 查看修改的文件
git status

# 添加所有你的修改
git add .

# 提交（写清楚本次做了什么）
git commit -m "feat(用户): 完成登录注册、个人中心、地址管理功能"

# 推送到远程你的功能分支
git push origin feature/user-module
```

然后在 Gitee 上创建 Pull Request，请求合并到 `develop` 分支，通知组长审查。

---

## 12. 常见问题与解决方案

### Q1: 启动时提示 `找不到 mapper` 或 `Invalid bound statement`
- 检查 `application.properties` 中 `mybatis.mapper-locations=classpath:mapper/*.xml`
- 确认 `UserMapper.xml` 和 `AddressMapper.xml` 放在 `src/main/resources/mapper/` 下，且 namespace 与接口全路径一致。

### Q2: 密码加密报错 `NoSuchMethodError`
- 确认 `pom.xml` 中有 `spring-security-crypto` 依赖，如果没有，手动添加（组长应已加）。

### Q3: 登录后跳转 `/dish/list` 找不到页面
- 因为组员B还没完成菜品模块，你可以暂时修改 `redirect:/user/profile` 测试。

### Q4: 我的订单页面需要调用其他模块，但对方还没完成怎么办？
- 先 mock：在 `UserController` 中临时返回一个空列表，让页面不报错。等组员D/E完成后，再替换为真实调用。

### Q5: 设置默认地址时，`clearDefaultByUserId` 执行了但是新地址不是默认？
- 检查 SQL：`clearDefaultByUserId` 是否执行成功；在 `addAddress` 方法中，调用顺序是否正确。

---

**下一步**：等你完成并提交代码后，组长会合并你的分支。然后你可以协助其他组员（比如帮助组员B调试购物车页面，或者帮助组员D测试地址选择功能）。