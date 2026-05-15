package com.pinenuts.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.pinenuts.dto.store.StoreCreateRequest;
import com.pinenuts.dto.store.StoreQueryRequest;
import com.pinenuts.dto.store.StoreResponse;
import com.pinenuts.dto.store.StoreUpdateRequest;

import java.util.List;

public interface StoreService {
    IPage<StoreResponse> getStoreList(Integer pageNum, Integer pageSize, StoreQueryRequest query);

    StoreResponse getStoreById(Long id);

    void createStore(StoreCreateRequest request);

    void updateStore(Long id, StoreUpdateRequest request);

    void deleteStore(Long id);

    List<StoreResponse> getAllStores();
}
