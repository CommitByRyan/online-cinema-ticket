CREATE DATABASE IF NOT EXISTS ordermeal DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE ordermeal;
-- ======================== 用户表 ========================
CREATE TABLE tb_user (
                         id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID，自增主键',
                         username VARCHAR(50) NOT NULL UNIQUE COMMENT '登录用户名，唯一',
                         password VARCHAR(100) NOT NULL COMMENT '密码（BCrypt加密后的密文）',
                         phone VARCHAR(20) NOT NULL COMMENT '手机号，用于订单号生成',
                         status VARCHAR(20) DEFAULT 'ACTIVE' COMMENT '状态：ACTIVE正常，BANNED封禁',
                         create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                         update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
);

-- ======================== 商家表 ========================
CREATE TABLE tb_merchant (
                             id BIGINT PRIMARY KEY AUTO_INCREMENT,
                             username VARCHAR(50) NOT NULL UNIQUE,
                             password VARCHAR(100) NOT NULL,
                             merchant_name VARCHAR(100) NOT NULL COMMENT '店铺名称',
                             legal_person VARCHAR(50) COMMENT '法人姓名',
                             phone VARCHAR(20) COMMENT '联系电话',
                             address VARCHAR(200) COMMENT '店铺地址',
                             business_license VARCHAR(50) COMMENT '营业执照号',
                             status VARCHAR(20) DEFAULT 'PENDING' COMMENT 'PENDING待审核，APPROVED已通过，BANNED封禁',
                             business_status VARCHAR(20) DEFAULT 'OPEN' COMMENT '营业状态：OPEN营业中，CLOSED休息中',
                             create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
                             update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- ======================== 菜品分类表 ========================
CREATE TABLE tb_dish_category (
                                  id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                  merchant_id BIGINT NOT NULL COMMENT '所属商家ID',
                                  category_name VARCHAR(50) NOT NULL COMMENT '分类名（如：热菜、凉菜）',
                                  sort_order INT DEFAULT 0 COMMENT '排序序号',
                                  FOREIGN KEY (merchant_id) REFERENCES tb_merchant(id) ON DELETE CASCADE
);

-- ======================== 菜品表 ========================
CREATE TABLE tb_dish (
                         id BIGINT PRIMARY KEY AUTO_INCREMENT,
                         merchant_id BIGINT NOT NULL,
                         category_id BIGINT COMMENT '所属分类ID，可为空',
                         dish_name VARCHAR(100) NOT NULL,
                         price DECIMAL(10,2) NOT NULL COMMENT '售价',
                         original_price DECIMAL(10,2) COMMENT '原价（用于显示折扣）',
                         image VARCHAR(500) COMMENT '图片路径',
                         description TEXT COMMENT '菜品描述',
                         status VARCHAR(20) DEFAULT 'ONLINE' COMMENT 'ONLINE上架，OFFLINE下架',
                         sales INT DEFAULT 0 COMMENT '销量（统计用）',
                         create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
                         update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                         FOREIGN KEY (merchant_id) REFERENCES tb_merchant(id),
                         FOREIGN KEY (category_id) REFERENCES tb_dish_category(id)
);

-- ======================== 收货地址表 ========================
CREATE TABLE tb_address (
                            id BIGINT PRIMARY KEY AUTO_INCREMENT,
                            user_id BIGINT NOT NULL,
                            receiver_name VARCHAR(50) NOT NULL COMMENT '收货人姓名',
                            phone VARCHAR(20) NOT NULL COMMENT '收货人电话',
                            province VARCHAR(50),
                            city VARCHAR(50),
                            district VARCHAR(50),
                            detail_address VARCHAR(200),
                            is_default TINYINT DEFAULT 0 COMMENT '是否默认地址：1是，0否',
                            FOREIGN KEY (user_id) REFERENCES tb_user(id) ON DELETE CASCADE
);

-- ======================== 订单表 ========================
CREATE TABLE tb_order (
                          id BIGINT PRIMARY KEY AUTO_INCREMENT,
                          order_no VARCHAR(50) NOT NULL UNIQUE COMMENT '订单号（唯一）',
                          user_id BIGINT NOT NULL,
                          merchant_id BIGINT NOT NULL,
                          total_amount DECIMAL(10,2) NOT NULL,
                          status VARCHAR(20) DEFAULT 'PENDING' COMMENT '订单状态：PENDING待支付，PAID已支付，CONFIRMED已接单，PREPARING制作中，DELIVERED配送中，COMPLETED已完成，CANCELLED已取消',
                          address_id BIGINT COMMENT '关联的地址ID',
                          remark VARCHAR(500) COMMENT '用户备注',
                          pay_time DATETIME,
                          confirm_time DATETIME,
                          deliver_time DATETIME,
                          complete_time DATETIME,
                          expire_time DATETIME COMMENT '支付过期时间（创建订单后15分钟）',
                          create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
                          FOREIGN KEY (user_id) REFERENCES tb_user(id),
                          FOREIGN KEY (merchant_id) REFERENCES tb_merchant(id),
                          FOREIGN KEY (address_id) REFERENCES tb_address(id)
);

-- ======================== 订单明细表 ========================
CREATE TABLE tb_order_item (
                               id BIGINT PRIMARY KEY AUTO_INCREMENT,
                               order_id BIGINT NOT NULL,
                               dish_id BIGINT NOT NULL,
                               dish_name VARCHAR(100) NOT NULL COMMENT '下单时的菜品名（快照）',
                               price DECIMAL(10,2) NOT NULL COMMENT '下单时的单价（快照）',
                               quantity INT NOT NULL,
                               subtotal DECIMAL(10,2) NOT NULL COMMENT '小计（price * quantity）',
                               FOREIGN KEY (order_id) REFERENCES tb_order(id) ON DELETE CASCADE,
                               FOREIGN KEY (dish_id) REFERENCES tb_dish(id)
);

-- ======================== 购物车表 ========================
CREATE TABLE tb_cart (
                         id BIGINT PRIMARY KEY AUTO_INCREMENT,
                         user_id BIGINT NOT NULL,
                         dish_id BIGINT NOT NULL,
                         quantity INT NOT NULL DEFAULT 1,
                         UNIQUE KEY uk_user_dish (user_id, dish_id) COMMENT '一个用户对一个菜品只能有一条记录',
                         FOREIGN KEY (user_id) REFERENCES tb_user(id),
                         FOREIGN KEY (dish_id) REFERENCES tb_dish(id)
);

-- ========== 索引（提升查询速度）==========
CREATE INDEX idx_username ON tb_user(username);
CREATE INDEX idx_phone ON tb_user(phone);
CREATE INDEX idx_order_user_id ON tb_order(user_id);
CREATE INDEX idx_order_merchant_id ON tb_order(merchant_id);
CREATE INDEX idx_order_status ON tb_order(status);
CREATE INDEX idx_order_expire_time ON tb_order(expire_time);

-- 插入普通用户（密码统一为 123456，经过 BCrypt 加密后的字符串）
-- 实际密码 123456 的 BCrypt 哈希是：$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOpw7Kc4n1jfC
INSERT INTO tb_user (username, password, phone, status) VALUES
                                                            ('user1', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOpw7Kc4n1jfC', '13811111111', 'ACTIVE'),
                                                            ('user2', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOpw7Kc4n1jfC', '13922222222', 'ACTIVE');

-- 插入商家（密码同上，状态 APPROVED 表示已审核通过）
INSERT INTO tb_merchant (username, password, merchant_name, legal_person, phone, address, business_license, status, business_status) VALUES
                                                                                                                                         ('merchant1', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOpw7Kc4n1jfC', '川味居', '李强', '18888888888', '成都市武侯区XX路', '91110105MA00XXXX1', 'APPROVED', 'OPEN'),
                                                                                                                                         ('merchant2', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOpw7Kc4n1jfC', '粤港轩', '王芳', '18899999999', '广州市天河区YY路', '91110105MA00XXXX2', 'APPROVED', 'OPEN');

-- 插入菜品分类
INSERT INTO tb_dish_category (merchant_id, category_name, sort_order) VALUES
                                                                          (1, '热菜', 1), (1, '凉菜', 2), (1, '汤类', 3);

-- 插入菜品
INSERT INTO tb_dish (merchant_id, category_id, dish_name, price, image, status, sales) VALUES
                                                                                           (1, 1, '宫保鸡丁', 38.00, '/images/gongbao.jpg', 'ONLINE', 120),
                                                                                           (1, 1, '水煮鱼', 68.00, '/images/shuizhuyu.jpg', 'ONLINE', 85),
                                                                                           (1, 2, '拍黄瓜', 12.00, '/images/huanggu.jpg', 'ONLINE', 200);

-- 插入管理员用户（角色不同，但也可以直接用 user 表，加一个 role 字段。为简化，单独建 admin 表？但题目要求用 user 表统一）
-- 我们给 user1 增加管理员权限？为了简单，我们在用户表加一个字段 role，但之前表没有。建议在 tb_user 增加 role 字段。
-- 为了方便，重新修改 tb_user 表添加 role 字段（执行以下 ALTER）
ALTER TABLE tb_user ADD COLUMN role VARCHAR(20) DEFAULT 'USER' COMMENT 'USER/ADMIN';
UPDATE tb_user SET role = 'ADMIN' WHERE username = 'user1';

SHOW COLUMNS FROM tb_user LIKE 'password';
SELECT id, username, password, LENGTH(password) AS len FROM tb_user WHERE username IN ('user1','user2');

UPDATE tb_user
SET password = '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOpw7Kc4n1jfC'
WHERE username IN ('user1','user2');


USE ordermeal;  -- 切换到项目库

ALTER TABLE tb_address
    ADD COLUMN create_time DATETIME DEFAULT CURRENT_TIMESTAMP AFTER detail_address,
    ADD COLUMN update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP AFTER create_time;

-- 为已经存在的记录补上时间（可选）
UPDATE tb_address SET create_time = NOW(), update_time = NOW() WHERE create_time IS NULL;
