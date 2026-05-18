package com.pinenuts.dto.dish;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class DishResponse {
    private Long id;
    private Long categoryId;
    private String categoryName;
    private String dishName;
    private String dishCode;
    private BigDecimal price;
    private String imageUrl;
    private String description;
    private String specifications;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
