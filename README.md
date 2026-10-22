# 在线点餐系统（online-ordermeal）

[![CI](https://github.com/CommitByRyan/online-cinema-ticket/actions/workflows/ci.yml/badge.svg)](https://github.com/CommitByRyan/online-cinema-ticket/actions/workflows/ci.yml)
![JDK](https://img.shields.io/badge/JDK-17%2B-informational)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-2.7.14-brightgreen)
![License](https://img.shields.io/badge/License-Apache%202.0-blue)

> **关于仓库名**：`online-cinema-ticket` 是项目初期的名称，后续需求变更为在线点餐系统，仓库名沿用至今。本项目实际内容为**在线点餐系统**。

基于 Spring Boot + MyBatis + Thymeleaf 的 O2O 点餐平台，覆盖三类角色：

- **普通用户**：注册登录、浏览菜品、购物车、下单支付、地址管理、订单查询
- **商家**：入驻审核、店铺信息、菜品与分类管理、订单状态流转
- **管理员**：商家审核与封禁、平台经营统计

## 功能特性

| 端 | 功能 |
| --- | --- |
| 用户端 | 登录/注册、个人中心（改手机号/密码）、收货地址管理（增删改、默认地址）、菜品分页与搜索、菜品详情、购物车（数量增减、总价计算）、确认订单、模拟支付、我的订单（多状态查询与取消） |
| 商家端 | 商家注册/登录（需管理员审核通过）、店铺信息维护、菜品分类管理、菜品上下架与编辑、订单接单→制作中→已完成状态流转 |
| 管理端 | 商家列表（审核通过/封禁/解封）、平台统计报表（用户数、商家数、订单量、营业额） |
| 系统能力 | 订单超时（15 分钟）自动取消定时任务、BCrypt 密码加密、统一异常处理、统一 JSON 返回格式、PageHelper 分页 |

## 技术栈

- **后端**：Spring Boot 2.7.14、Spring MVC、MyBatis 2.3.0、PageHelper 1.4.6、Spring Security Crypto（BCrypt）、Spring `@Scheduled` 定时任务
- **前端**：Thymeleaf、Bootstrap 5、jQuery
- **数据库**：MySQL 8.0+
- **构建**：Maven（仓库内置 Maven Wrapper，无需单独安装 Maven）

## 快速开始

### 环境要求

- JDK 17+
- MySQL 8.0+
- Maven 3.8+（或直接使用仓库自带的 `./mvnw`）

### 1. 克隆仓库

```bash
git clone https://github.com/CommitByRyan/online-cinema-ticket.git
cd online-cinema-ticket
```

### 2. 初始化数据库

执行 `doc/init.sql`（建表 + 测试数据 + 索引，共 8 张表）：

```bash
mysql -h 127.0.0.1 -P 3307 -uroot -p < doc/init.sql
```

### 3. 配置数据库连接

默认配置位于 `src/main/resources/application.properties`，连接本地 **3307** 端口的 `ordermeal` 库（用户名/密码默认 `root`）。

如你的环境不同，无需改文件，用环境变量覆盖即可：

```bash
export DB_URL="jdbc:mysql://localhost:3306/ordermeal?serverTimezone=Asia/Shanghai&useSSL=false&characterEncoding=utf8"
export DB_USER="root"
export DB_PASSWORD="你的密码"
```

### 4. 启动

```bash
./mvnw spring-boot:run
```

或打包后运行：

```bash
./mvnw clean package
java -jar target/olineordermeal-0.0.1-SNAPSHOT.jar
```

### 5. 访问入口

| 端 | 地址 |
| --- | --- |
| 用户端（首页/登录） | <http://localhost:8080> |
| 商家端 | <http://localhost:8080/merchant/login> |
| 管理端 | <http://localhost:8080/admin/merchant/list> |

> 管理端没有独立登录页：先用**具有 ADMIN 角色的用户账号**在用户登录页登录，再访问管理端地址。`doc/init.sql` 已把 `user1` 设为 ADMIN。

### 测试账号（密码均为 `123456`）

| 角色 | 账号 | 说明 |
| --- | --- | --- |
| 普通用户 | `user1` / `user2` | `user1` 同时具备 ADMIN 角色 |
| 商家（已审核） | `merchant1` / `merchant2` | 川味居 / 粤港轩，均处于营业中 |

## 项目结构

```text
online-cinema-ticket/
├── doc/
│   └── init.sql                     # 数据库初始化脚本（建表 + 索引 + 测试数据）
├── src/main/java/com/group7/olineordermeal/
│   ├── common/                      # 配置、枚举、异常、统一返回、工具（订单状态机等）
│   ├── controller/                  # 控制器：认证、用户、订单
│   │   ├── admin/                   # 管理端：商家审核、统计报表、菜品、购物车
│   │   └── merchant/                # 商家端：认证、店铺、菜品分类、菜品、订单
│   ├── dto/                         # 请求参数对象
│   ├── entity/                      # 实体类（对应 8 张业务表）
│   ├── mapper/                      # MyBatis 接口
│   ├── service/ + service/impl/     # 业务层
│   └── task/                        # 定时任务：待支付订单超时自动取消
├── src/main/resources/
│   ├── mapper/                      # MyBatis XML
│   ├── static/                      # CSS/JS/图片
│   ├── templates/                   # Thymeleaf 模板（三类角色各自目录）
│   ├── application.properties       # 应用配置（支持环境变量覆盖）
│   └── logback-spring.xml           # 日志配置
├── src/test/                        # 单元测试
├── Group_7_project/                 # 小组协作过程文档（各组指导手册）
├── 团队协作规则手册.md               # 团队协作规范
└── pom.xml
```

## 核心业务规则

- **订单状态机**：待支付 → 已支付 → 商家已接单 → 制作中 → 已完成；待支付超过 15 分钟由定时任务自动取消（每 30 秒扫描一次）。
- **购物车规则**：只允许存放同一商家的菜品；加入不同商家的菜品时会提示清空购物车。
- **商家入驻**：注册后状态为待审核，须管理员审核通过才能登录经营。
- **模拟支付**：点击"模拟支付"即变更订单为已支付，无真实资金流转。
- **图片上传**：菜品图片保存至 `upload/images/`（运行时目录，已加入 `.gitignore`）。

## 主要路由一览

| 控制器 | 路径前缀 | 职责 |
| --- | --- | --- |
| `AuthController` | `/login`、`/register`、`/logout` | 用户认证 |
| `UserController` | `/user` | 个人中心、地址、我的订单 |
| `controller.admin.DishController` | `/dish` | 菜品列表/详情/搜索 |
| `controller.admin.CartController` | `/cart` | 购物车 |
| `OrderController` | `/order` | 下单、支付、取消、详情 |
| `merchant.MerchantAuthController` | `/merchant` | 商家登录/注册/首页 |
| `merchant.MerchantInfoController` | `/merchant/info` | 店铺信息 |
| `merchant.MerchantCategoryController` | `/merchant/category` | 菜品分类 |
| `merchant.MerchantDishController` | `/merchant/dish` | 菜品管理 |
| `merchant.MerchantOrderController` | `/merchant/order` | 商家订单处理 |
| `admin.AdminMerchantController` | `/admin/merchant` | 商家审核/封禁/解封 |
| `admin.AdminStatisticsController` | `/admin` | 平台统计 |

## 持续集成

仓库内置 GitHub Actions 工作流（`.github/workflows/ci.yml`）：

1. 启动 MySQL 8.0 服务容器并执行 `doc/init.sql`；
2. 使用 JDK 17 执行 `./mvnw -B clean verify`（编译 + 测试 + 打包）。

推送代码或提交 PR 时自动运行，工作流状态见仓库首页徽章。

## 开发规范

- 接口返回统一格式 `{code, msg, data}`（`common.result.Result`）。
- 数据库表名以 `tb_` 为前缀，字段名小写下划线分隔；实体类属性用驼峰，由 MyBatis 自动映射。
- 提交代码前确保 `./mvnw clean compile` 通过。
- 文本文件统一 LF 换行（`.gitattributes` 已配置）。

## 常见问题

**Q：启动报 `Access denied for user 'root'@'localhost'`？**
A：MySQL 密码不是默认的 `root`，请用 `DB_PASSWORD` 环境变量覆盖，或直接修改 `application.properties`。

**Q：启动报 `Unknown database 'ordermeal'`？**
A：先执行 `doc/init.sql` 建库建表；脚本默认库名为 `ordermeal`。

**Q：图片上传后 404？**
A：上传目录 `upload/images/` 是运行时生成的，确保以项目根目录为工作目录启动；重启后历史图片不丢失。

**Q：MySQL 不在 3307 端口？**
A：设置 `DB_URL` 环境变量指向实际端口即可，无需改代码。

## 许可证

本项目基于 [Apache License 2.0](LICENSE) 开源。
