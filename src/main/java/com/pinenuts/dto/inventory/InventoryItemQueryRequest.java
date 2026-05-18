package com.pinenuts.dto.inventory;

import lombok.Data;

@Data
public class InventoryItemQueryRequest {
    private String itemName;
    private String category;
    private Long storeId;
    private Integer status;
    private Boolean lowStock;  // 是否只显示低库存
}
