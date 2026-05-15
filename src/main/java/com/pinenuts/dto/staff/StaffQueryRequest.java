package com.pinenuts.dto.staff;

import lombok.Data;

@Data
public class StaffQueryRequest {
    private String staffName;
    private Long storeId;
    private Integer status;
}
