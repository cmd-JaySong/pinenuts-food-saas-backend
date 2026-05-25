package com.pinenuts.dto.purchase;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class PurchaseOrderListResponse {
    private Long id;
    private String purchaseCode;
    private Long storeId;
    private String storeName;
    private Integer status;
    private BigDecimal totalAmount;
    private String remark;
    private Long applicantId;
    private String applicantName;
    private String approverName;
    private LocalDateTime approvalTime;
    private LocalDateTime createdAt;
}
