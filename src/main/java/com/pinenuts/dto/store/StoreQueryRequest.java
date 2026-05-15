package com.pinenuts.dto.store;

import lombok.Data;

@Data
public class StoreQueryRequest {
    private String storeName;
    private String storeCode;
    private Integer status;
}
