package com.pinenuts.dto.purchase;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.List;

@Data
public class PurchaseOrderUpdateRequest {
    @Size(max = 500, message = "备注不超过500字符")
    private String remark;

    @NotEmpty(message = "采购明细不能为空")
    @Valid
    private List<PurchaseOrderItemDTO> items;
}
