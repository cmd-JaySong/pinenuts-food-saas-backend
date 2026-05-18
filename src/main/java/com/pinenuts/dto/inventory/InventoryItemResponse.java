package com.pinenuts.dto.inventory;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class InventoryItemResponse {
    private Long id;
    private Long storeId;
    private String storeName;
    private String itemName;
    private String itemCode;
    private String category;
    private String unit;
    private BigDecimal quantity;
    private BigDecimal costPrice;
    private BigDecimal alertThreshold;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
