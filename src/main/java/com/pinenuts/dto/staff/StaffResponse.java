package com.pinenuts.dto.staff;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class StaffResponse {
    private Long id;
    private Long storeId;
    private String storeName;
    private String staffName;
    private String phone;
    private String position;
    private LocalDate entryDate;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
