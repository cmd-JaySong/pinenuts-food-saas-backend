package com.pinenuts.dto.staff;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class StaffUpdateRequest {
    private Long storeId;

    @NotBlank(message = "员工姓名不能为空")
    @Size(max = 50, message = "员工姓名长度不能超过50")
    private String staffName;

    private String phone;
    private String position;
    private LocalDate entryDate;
    private Integer status;
}
