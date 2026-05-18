package com.pinenuts.dto.dish;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class DishCreateRequest {
    @NotNull(message = "分类不能为空")
    private Long categoryId;

    @NotBlank(message = "菜品名称不能为空")
    @Size(max = 100)
    private String dishName;

    @Size(max = 50)
    private String dishCode;

    @NotNull(message = "价格不能为空")
    @DecimalMin(value = "0.01", message = "价格必须大于0")
    private BigDecimal price;

    private String imageUrl;

    @Size(max = 500)
    private String description;

    private String specifications;
}
