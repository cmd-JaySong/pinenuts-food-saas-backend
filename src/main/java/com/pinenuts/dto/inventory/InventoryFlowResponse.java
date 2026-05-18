package com.pinenuts.dto.inventory;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class InventoryFlowResponse {
    private Long id;
    private Long itemId;
    private String itemName;
    private Integer flowType;
    private String sourceType;
    private BigDecimal quantity;
    private BigDecimal beforeQuantity;
    private BigDecimal afterQuantity;
    private String operatorName;
    private String remark;
    private LocalDateTime createdAt;
}
