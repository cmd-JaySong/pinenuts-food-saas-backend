package com.pinenuts.dto.inventory;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class InventoryItemUpdateRequest {
    @Size(max = 100, message = "物料名称不超过100字符")
    private String itemName;

    @Size(max = 50, message = "物料分类不超过50字符")
    private String category;

    @Size(max = 20, message = "计量单位不超过20字符")
    private String unit;

    @DecimalMin(value = "0", message = "成本单价不能为负数")
    private BigDecimal costPrice;

    @DecimalMin(value = "0", message = "预警阈值不能为负数")
    private BigDecimal alertThreshold;

    private Integer status;
}
