package com.group7.olineordermeal.dto;

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