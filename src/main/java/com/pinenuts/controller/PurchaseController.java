package com.pinenuts.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.pinenuts.common.Result;
import com.pinenuts.common.annotation.RequiresPermission;
import com.pinenuts.dto.purchase.*;
import com.pinenuts.service.PurchaseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/purchase")
@RequiredArgsConstructor
@Tag(name = "采购管理", description = "采购管理相关接口")
public class PurchaseController {

    private final PurchaseService purchaseService;

    @GetMapping("/list")
    @Operation(summary = "采购单列表")
    @RequiresPermission("purchase:list")
    public Result<IPage<PurchaseOrderListResponse>> list(
            PurchaseOrderQueryRequest query,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(purchaseService.getList(query, pageNum, pageSize));
    }

    @GetMapping("/{id}")
    @Operation(summary = "采购单详情")
    @RequiresPermission("purchase:list")
    public Result<PurchaseOrderResponse> getById(@PathVariable Long id) {
        return Result.success(purchaseService.getById(id));
    }

    @PostMapping
    @Operation(summary = "创建采购单")
    @RequiresPermission("purchase:create")
    public Result<Void> create(@Valid @RequestBody PurchaseOrderCreateRequest request) {
        purchaseService.create(request);
        return Result.success();
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新采购单")
    @RequiresPermission("purchase:create")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody PurchaseOrderUpdateRequest request) {
        purchaseService.update(id, request);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除采购单")
    @RequiresPermission("purchase:delete")
    public Result<Void> delete(@PathVariable Long id) {
        purchaseService.delete(id);
        return Result.success();
    }

    @PostMapping("/{id}/submit")
    @Operation(summary = "提交审批")
    @RequiresPermission("purchase:submit")
    public Result<Void> submit(@PathVariable Long id) {
        purchaseService.submit(id);
        return Result.success();
    }

    @PostMapping("/{id}/withdraw")
    @Operation(summary = "撤回申请")
    @RequiresPermission("purchase:withdraw")
    public Result<Void> withdraw(@PathVariable Long id) {
        purchaseService.withdraw(id);
        return Result.success();
    }

    @PostMapping("/{id}/approve")
    @Operation(summary = "审批通过")
    @RequiresPermission("purchase:approve")
    public Result<Void> approve(@PathVariable Long id, @RequestBody(required = false) PurchaseApproveRequest request) {
        purchaseService.approve(id, request != null ? request : new PurchaseApproveRequest());
        return Result.success();
    }

    @PostMapping("/{id}/reject")
    @Operation(summary = "审批驳回")
    @RequiresPermission("purchase:approve")
    public Result<Void> reject(@PathVariable Long id, @Valid @RequestBody PurchaseApproveRequest request) {
        purchaseService.reject(id, request);
        return Result.success();
    }
}
