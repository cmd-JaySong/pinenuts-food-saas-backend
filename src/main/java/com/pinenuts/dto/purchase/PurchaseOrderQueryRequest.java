package com.pinenuts.dto.purchase;

import lombok.Data;

@Data
public class PurchaseOrderQueryRequest {
    private String purchaseCode;
    private Long storeId;
    private Integer status;
    private String startTime;
    private String endTime;
}
