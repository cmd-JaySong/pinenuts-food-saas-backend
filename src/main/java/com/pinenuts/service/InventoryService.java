package com.pinenuts.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.pinenuts.dto.inventory.*;

public interface InventoryService {
    IPage<InventoryItemResponse> getInventoryList(InventoryItemQueryRequest query, Integer pageNum, Integer pageSize);
    InventoryItemResponse getInventoryById(Long id);
    void createInventoryItem(InventoryItemCreateRequest request);
    void updateInventoryItem(Long id, InventoryItemUpdateRequest request);
    void deleteInventoryItem(Long id);
    void inbound(InventoryInboundRequest request);
    void outbound(InventoryOutboundRequest request);
    void stockCheck(InventoryCheckRequest request);
    IPage<InventoryFlowResponse> getFlowList(InventoryFlowQueryRequest query, Integer pageNum, Integer pageSize);
    IPage<InventoryAlertResponse> getAlertList(Integer status, Integer pageNum, Integer pageSize);
    void handleAlert(Long id);
}
