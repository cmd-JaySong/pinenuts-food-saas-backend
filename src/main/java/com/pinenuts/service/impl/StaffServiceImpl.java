package com.pinenuts.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pinenuts.common.ResultCode;
import com.pinenuts.common.exception.BusinessException;
import com.pinenuts.dto.staff.StaffCreateRequest;
import com.pinenuts.dto.staff.StaffQueryRequest;
import com.pinenuts.dto.staff.StaffResponse;
import com.pinenuts.dto.staff.StaffUpdateRequest;
import com.pinenuts.entity.Staff;
import com.pinenuts.entity.Store;
import com.pinenuts.mapper.StaffMapper;
import com.pinenuts.mapper.StoreMapper;
import com.pinenuts.service.StaffService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class StaffServiceImpl implements StaffService {

    private final StaffMapper staffMapper;
    private final StoreMapper storeMapper;

    @Override
    public IPage<StaffResponse> getStaffList(Integer pageNum, Integer pageSize, StaffQueryRequest query) {
        LambdaQueryWrapper<Staff> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(query.getStaffName()), Staff::getStaffName, query.getStaffName())
                .eq(query.getStoreId() != null, Staff::getStoreId, query.getStoreId())
                .eq(query.getStatus() != null, Staff::getStatus, query.getStatus())
                .orderByDesc(Staff::getCreatedAt);

        Page<Staff> page = staffMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);

        return page.convert(this::convertToResponse);
    }

    @Override
    public StaffResponse getStaffById(Long id) {
        Staff staff = staffMapper.selectById(id);
        if (staff == null) {
            throw new BusinessException(ResultCode.STAFF_NOT_FOUND);
        }
        return convertToResponse(staff);
    }

    @Override
    public void createStaff(StaffCreateRequest request) {
        // 校验门店存在
        Store store = storeMapper.selectById(request.getStoreId());
        if (store == null) {
            throw new BusinessException(ResultCode.STORE_NOT_FOUND);
        }

        Staff staff = new Staff();
        staff.setStoreId(request.getStoreId());
        staff.setStaffName(request.getStaffName());
        staff.setPhone(request.getPhone());
        staff.setPosition(request.getPosition());
        staff.setEntryDate(request.getEntryDate());
        staff.setUserId(request.getUserId());
        staff.setStatus(1);

        staffMapper.insert(staff);
        log.info("新增员工成功: {}", staff.getStaffName());
    }

    @Override
    public void updateStaff(Long id, StaffUpdateRequest request) {
        Staff staff = staffMapper.selectById(id);
        if (staff == null) {
            throw new BusinessException(ResultCode.STAFF_NOT_FOUND);
        }

        // 如果 storeId 改变，校验新门店是否存在
        if (request.getStoreId() != null && !request.getStoreId().equals(staff.getStoreId())) {
            Store store = storeMapper.selectById(request.getStoreId());
            if (store == null) {
                throw new BusinessException(ResultCode.STORE_NOT_FOUND);
            }
            staff.setStoreId(request.getStoreId());
        }

        staff.setStaffName(request.getStaffName());
        staff.setPhone(request.getPhone());
        staff.setPosition(request.getPosition());
        staff.setEntryDate(request.getEntryDate());
        if (request.getStatus() != null) {
            staff.setStatus(request.getStatus());
        }

        staffMapper.updateById(staff);
        log.info("更新员工成功: id={}", id);
    }

    @Override
    public void deleteStaff(Long id) {
        Staff staff = staffMapper.selectById(id);
        if (staff == null) {
            throw new BusinessException(ResultCode.STAFF_NOT_FOUND);
        }
        staffMapper.deleteById(id);
        log.info("删除员工成功: id={}", id);
    }

    private StaffResponse convertToResponse(Staff staff) {
        String storeName = null;
        if (staff.getStoreId() != null) {
            Store store = storeMapper.selectById(staff.getStoreId());
            if (store != null) {
                storeName = store.getStoreName();
            }
        }

        return StaffResponse.builder()
                .id(staff.getId())
                .storeId(staff.getStoreId())
                .storeName(storeName)
                .staffName(staff.getStaffName())
                .phone(staff.getPhone())
                .position(staff.getPosition())
                .entryDate(staff.getEntryDate())
                .status(staff.getStatus())
                .createdAt(staff.getCreatedAt())
                .updatedAt(staff.getUpdatedAt())
                .build();
    }
}
