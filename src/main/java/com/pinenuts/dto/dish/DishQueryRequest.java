package com.pinenuts.dto.dish;

import lombok.Data;

@Data
public class DishQueryRequest {
    private String dishName;
    private Long categoryId;
    private Integer status;
}
