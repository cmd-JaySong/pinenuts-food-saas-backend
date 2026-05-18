package com.pinenuts.dto.inventory;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class InventoryAlertResponse {
    private Long id;
    private Long storeId;
    private String storeName;
    private Long itemId;
    private String itemName;
    private BigDecimal currentQuantity;
    private BigDecimal alertThreshold;
    private Integer status;
    private LocalDateTime createdAt;
}
