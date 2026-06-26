package com.group7.olineordermeal.dto;

import lombok.Data;

@Data
public class CategoryDTO {
    private Long id;
    private String categoryName;
    private Integer sortOrder;
}