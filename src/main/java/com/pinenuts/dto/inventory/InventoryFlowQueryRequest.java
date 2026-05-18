package com.pinenuts.dto.inventory;

import lombok.Data;

@Data
public class InventoryFlowQueryRequest {
    private Long itemId;
    private Integer flowType;
    private String startTime;
    private String endTime;
}
