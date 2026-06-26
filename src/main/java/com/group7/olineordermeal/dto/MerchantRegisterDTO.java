package com.group7.olineordermeal.dto;

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