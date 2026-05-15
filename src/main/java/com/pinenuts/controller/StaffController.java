package com.pinenuts.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.pinenuts.common.Result;
import com.pinenuts.common.annotation.RequiresPermission;
import com.pinenuts.dto.staff.StaffCreateRequest;
import com.pinenuts.dto.staff.StaffQueryRequest;
import com.pinenuts.dto.staff.StaffResponse;
import com.pinenuts.dto.staff.StaffUpdateRequest;
import com.pinenuts.service.StaffService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/staff")
@RequiredArgsConstructor
@Tag(name = "员工管理", description = "员工CRUD接口")
public class StaffController {

    private final StaffService staffService;

    @GetMapping("/list")
    @Operation(summary = "员工列表（分页）")
    @RequiresPermission("staff:list")
    public Result<IPage<StaffResponse>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            StaffQueryRequest query) {
        return Result.success(staffService.getStaffList(pageNum, pageSize, query));
    }

    @GetMapping("/{id}")
    @Operation(summary = "员工详情")
    @RequiresPermission("staff:list")
    public Result<StaffResponse> getById(@PathVariable Long id) {
        return Result.success(staffService.getStaffById(id));
    }

    @PostMapping
    @Operation(summary = "新增员工")
    @RequiresPermission("staff:create")
    public Result<Void> create(@Valid @RequestBody StaffCreateRequest request) {
        staffService.createStaff(request);
        return Result.success();
    }

    @PutMapping("/{id}")
    @Operation(summary = "修改员工")
    @RequiresPermission("staff:update")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody StaffUpdateRequest request) {
        staffService.updateStaff(id, request);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除员工")
    @RequiresPermission("staff:delete")
    public Result<Void> delete(@PathVariable Long id) {
        staffService.deleteStaff(id);
        return Result.success();
    }
}
