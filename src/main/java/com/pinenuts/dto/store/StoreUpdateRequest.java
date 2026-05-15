package com.pinenuts.dto.store;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class StoreUpdateRequest {
    @NotBlank(message = "门店名称不能为空")
    @Size(max = 100, message = "门店名称长度不能超过100")
    private String storeName;

    @Size(max = 255, message = "地址长度不能超过255")
    private String address;

    private String contactPhone;
    private String businessHours;
    private Integer status;
}
