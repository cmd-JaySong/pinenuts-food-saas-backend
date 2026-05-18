package com.pinenuts.dto.dish;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class DishUpdateRequest {
    private Long categoryId;

    @NotBlank(message = "菜品名称不能为空")
    @Size(max = 100)
    private String dishName;

    private String dishCode;

    @NotNull(message = "价格不能为空")
    @DecimalMin(value = "0.01")
    private BigDecimal price;

    private String imageUrl;

    @Size(max = 500)
    private String description;

    private String specifications;
    private Integer status;
}
