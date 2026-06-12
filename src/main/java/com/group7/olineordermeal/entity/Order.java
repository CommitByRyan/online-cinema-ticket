package com.group7.olineordermeal.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data  // Lombok 注解，自动生成 getter/setter/toString
public class Order {
    private Long id;
    private String orderNo;
    private Long userId;
    private Long merchantId;
    private BigDecimal totalAmount;
    private String status;      // 数据库存储字符串，如 "PENDING"
    private Long addressId;
    private String remark;
    private LocalDateTime payTime;
    private LocalDateTime confirmTime;
    private LocalDateTime deliverTime;
    private LocalDateTime completeTime;
    private LocalDateTime expireTime;
    private LocalDateTime createTime;
}