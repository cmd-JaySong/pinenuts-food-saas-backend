package com.pinenuts.dto.report;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StoreRankingResponse {
    private Long storeId;
    private String storeName;
    private BigDecimal totalRevenue;
    private Integer orderCount;
}
