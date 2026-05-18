package com.pinenuts.dto.inventory;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class InventoryCheckRequest {
    @NotNull(message = "物料ID不能为空")
    private Long itemId;

    @NotNull(message = "实际库存量不能为空")
    @DecimalMin(value = "0", message = "实际库存量不能为负数")
    private BigDecimal actualQuantity;

    @Size(max = 255, message = "备注不超过255字符")
    private String remark;
}
