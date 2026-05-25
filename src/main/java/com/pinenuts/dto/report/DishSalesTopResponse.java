package com.pinenuts.dto.report;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DishSalesTopResponse {
    private Long dishId;
    private String dishName;
    private Integer quantity;
    private BigDecimal revenue;
}
