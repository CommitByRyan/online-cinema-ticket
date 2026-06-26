package com.group7.olineordermeal.entity;

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