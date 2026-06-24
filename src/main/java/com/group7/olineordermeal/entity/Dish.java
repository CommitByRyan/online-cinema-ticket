package com.group7.olineordermeal.entity;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 菜品实体，对应表 tb_dish
 */
@Data
public class Dish {
    private Long id;
    private Long merchantId;      // 所属商家ID
    private Long categoryId;      // 分类ID（可为空）
    private String dishName;
    private String merchantName;// 菜品名
    private BigDecimal price;     // 售价
    private BigDecimal originalPrice; // 原价
    private String image;         // 图片路径
    private String description;   // 描述
    private String status;        // ONLINE / OFFLINE
    private Integer sales;        // 销量
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    // 注意：为了展示方便，可以额外加一个字段 merchantName，但不在数据库表中
    // 可以在查询时用联表查询填充
}
