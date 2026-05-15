package com.pinenuts.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pinenuts.common.ResultCode;
import com.pinenuts.common.exception.BusinessException;
import com.pinenuts.dto.store.StoreCreateRequest;
import com.pinenuts.dto.store.StoreQueryRequest;
import com.pinenuts.dto.store.StoreResponse;
import com.pinenuts.dto.store.StoreUpdateRequest;
import com.pinenuts.entity.Store;
import com.pinenuts.mapper.StoreMapper;
import com.pinenuts.service.StoreService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class StoreServiceImpl implements StoreService {

    private final StoreMapper storeMapper;

    @Override
    public IPage<StoreResponse> getStoreList(Integer pageNum, Integer pageSize, StoreQueryRequest query) {
        LambdaQueryWrapper<Store> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(query.getStoreName()), Store::getStoreName, query.getStoreName())
                .like(StringUtils.hasText(query.getStoreCode()), Store::getStoreCode, query.getStoreCode())
                .eq(query.getStatus() != null, Store::getStatus, query.getStatus())
                .orderByDesc(Store::getCreatedAt);

        Page<Store> page = storeMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);

        return page.convert(this::convertToResponse);
    }

    @Override
    public StoreResponse getStoreById(Long id) {
        Store store = storeMapper.selectById(id);
        if (store == null) {
            throw new BusinessException(ResultCode.STORE_NOT_FOUND);
        }
        return convertToResponse(store);
    }

    @Override
    public void createStore(StoreCreateRequest request) {
        // 校验 storeCode 在当前租户下是否重复
        LambdaQueryWrapper<Store> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Store::getStoreCode, request.getStoreCode());
        if (storeMapper.selectCount(wrapper) > 0) {
            throw new BusinessException(ResultCode.STORE_CODE_DUPLICATE);
        }

        Store store = new Store();
        store.setStoreCode(request.getStoreCode());
        store.setStoreName(request.getStoreName());
        store.setAddress(request.getAddress());
        store.setContactPhone(request.getContactPhone());
        store.setBusinessHours(request.getBusinessHours());
        store.setStatus(1);

        storeMapper.insert(store);
        log.info("新增门店成功: {}", store.getStoreCode());
    }

    @Override
    public void updateStore(Long id, StoreUpdateRequest request) {
        Store store = storeMapper.selectById(id);
        if (store == null) {
            throw new BusinessException(ResultCode.STORE_NOT_FOUND);
        }

        store.setStoreName(request.getStoreName());
        store.setAddress(request.getAddress());
        store.setContactPhone(request.getContactPhone());
        store.setBusinessHours(request.getBusinessHours());
        if (request.getStatus() != null) {
            store.setStatus(request.getStatus());
        }

        storeMapper.updateById(store);
        log.info("更新门店成功: id={}", id);
    }

    @Override
    public void deleteStore(Long id) {
        Store store = storeMapper.selectById(id);
        if (store == null) {
            throw new BusinessException(ResultCode.STORE_NOT_FOUND);
        }
        storeMapper.deleteById(id);
        log.info("删除门店成功: id={}", id);
    }

    @Override
    public List<StoreResponse> getAllStores() {
        LambdaQueryWrapper<Store> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Store::getStatus, 1)
                .orderByAsc(Store::getStoreCode);
        List<Store> stores = storeMapper.selectList(wrapper);
        return stores.stream().map(this::convertToResponse).collect(Collectors.toList());
    }

    private StoreResponse convertToResponse(Store store) {
        return StoreResponse.builder()
                .id(store.getId())
                .storeCode(store.getStoreCode())
                .storeName(store.getStoreName())
                .address(store.getAddress())
                .contactPhone(store.getContactPhone())
                .businessHours(store.getBusinessHours())
                .status(store.getStatus())
                .createdAt(store.getCreatedAt())
                .updatedAt(store.getUpdatedAt())
                .build();
    }
}
