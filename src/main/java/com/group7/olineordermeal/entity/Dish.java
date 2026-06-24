package com.group7.olineordermeal.entity;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class Dish {
    private Long id;
    private Long merchantId;      // 所属商家ID
    private Long categoryId;      // 分类ID（可为空）
    private String dishName;      // 菜品名
    private BigDecimal price;     // 售价
    private BigDecimal originalPrice; // 原价
    private String image;         // 图片路径
    private String description;   // 描述
    private String status;        // ONLINE / OFFLINE
    private Integer sales;        // 销量
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private String merchantName; // 所属商家名称

    // 注意：为了展示方便，可以额外加一个字段 merchantName，但不在数据库表中
    // 可以在查询时用联表查询填充
}
