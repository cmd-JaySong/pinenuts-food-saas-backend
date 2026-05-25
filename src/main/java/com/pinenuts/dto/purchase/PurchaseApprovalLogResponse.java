package com.pinenuts.dto.purchase;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class PurchaseApprovalLogResponse {
    private Long id;
    private Integer action;  // 1-创建 2-提交 3-撤回 4-通过 5-驳回
    private String operatorName;
    private String remark;
    private LocalDateTime createdAt;
}
