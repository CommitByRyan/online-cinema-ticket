package com.group7.olineordermeal.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Merchant {
    private Long id;
    private String username;
    private String password;
    private String merchantName;
    private String legalPerson;
    private String phone;
    private String address;
    private String businessLicense;
    private String status;       // PENDING, APPROVED, BANNED
    private String businessStatus; // OPEN, CLOSED
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}