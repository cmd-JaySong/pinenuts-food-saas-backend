package com.pinenuts.dto.store;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class StoreResponse {
    private Long id;
    private String storeCode;
    private String storeName;
    private String address;
    private String contactPhone;
    private String businessHours;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
