package com.pinenuts.dto.purchase;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class PurchaseOrderResponse {
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
    private String approvalRemark;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<PurchaseOrderItemDTO> items;
    private List<PurchaseApprovalLogResponse> approvalLogs;
}
