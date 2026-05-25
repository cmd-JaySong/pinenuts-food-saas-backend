package com.pinenuts.dto.purchase;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PurchaseOrderItemDTO {
    private Long itemId;  // 可为空（新物料）

    @NotBlank(message = "物料名称不能为空")
    @Size(max = 100, message = "物料名称不超过100字符")
    private String itemName;

    @NotBlank(message = "单位不能为空")
    @Size(max = 20, message = "单位不超过20字符")
    private String unit;

    @NotNull(message = "采购数量不能为空")
    @DecimalMin(value = "0.01", message = "采购数量必须大于0")
    private BigDecimal quantity;

    @DecimalMin(value = "0", message = "单价不能为负数")
    private BigDecimal unitPrice;

    // 响应时用到的字段（请求时忽略）
    private BigDecimal totalPrice;
    private Long id;  // 明细行ID（更新时用）
}
