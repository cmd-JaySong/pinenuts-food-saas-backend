package com.pinenuts.dto.inventory;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class InventoryInboundRequest {
    @NotNull(message = "物料ID不能为空")
    private Long itemId;

    @NotNull(message = "入库数量不能为空")
    @DecimalMin(value = "0.01", message = "入库数量必须大于0")
    private BigDecimal quantity;

    @Size(max = 30, message = "来源类型不超过30字符")
    private String sourceType;  // purchase_in / manual_in

    @Size(max = 255, message = "备注不超过255字符")
    private String remark;
}
