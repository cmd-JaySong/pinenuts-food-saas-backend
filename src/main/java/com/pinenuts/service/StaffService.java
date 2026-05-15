package com.pinenuts.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.pinenuts.dto.staff.StaffCreateRequest;
import com.pinenuts.dto.staff.StaffQueryRequest;
import com.pinenuts.dto.staff.StaffResponse;
import com.pinenuts.dto.staff.StaffUpdateRequest;

public interface StaffService {
    IPage<StaffResponse> getStaffList(Integer pageNum, Integer pageSize, StaffQueryRequest query);

    StaffResponse getStaffById(Long id);

    void createStaff(StaffCreateRequest request);

    void updateStaff(Long id, StaffUpdateRequest request);

    void deleteStaff(Long id);
}
