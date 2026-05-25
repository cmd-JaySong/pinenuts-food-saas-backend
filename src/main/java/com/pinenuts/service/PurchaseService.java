package com.pinenuts.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.pinenuts.dto.purchase.*;

public interface PurchaseService {
    IPage<PurchaseOrderListResponse> getList(PurchaseOrderQueryRequest query, Integer pageNum, Integer pageSize);
    PurchaseOrderResponse getById(Long id);
    void create(PurchaseOrderCreateRequest request);
    void update(Long id, PurchaseOrderUpdateRequest request);
    void delete(Long id);
    void submit(Long id);
    void withdraw(Long id);
    void approve(Long id, PurchaseApproveRequest request);
    void reject(Long id, PurchaseApproveRequest request);
}
