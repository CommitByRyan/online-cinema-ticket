package com.group7.olineordermeal.dto;

import lombok.Data;

@Data
public class OrderStatusUpdateDTO {
    private Long orderId;
    private String targetStatus;  // 例如 "CONFIRMED"
}