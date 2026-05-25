package com.pinenuts.dto.purchase;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class PurchaseApproveRequest {
    @Size(max = 500, message = "审批意见不超过500字符")
    private String remark;
}
