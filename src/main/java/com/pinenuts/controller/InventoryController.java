package com.pinenuts.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.pinenuts.common.Result;
import com.pinenuts.common.annotation.RequiresPermission;
import com.pinenuts.dto.inventory.*;
import com.pinenuts.service.InventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
@Tag(name = "库存管理", description = "库存管理相关接口")
public class InventoryController {

    private final InventoryService inventoryService;

    @GetMapping("/list")
    @Operation(summary = "库存台账列表")
    @RequiresPermission("inventory:list")
    public Result<IPage<InventoryItemResponse>> list(
            InventoryItemQueryRequest query,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(inventoryService.getInventoryList(query, pageNum, pageSize));
    }

    @GetMapping("/{id}")
    @Operation(summary = "物料详情")
    @RequiresPermission("inventory:list")
    public Result<InventoryItemResponse> getById(@PathVariable Long id) {
        return Result.success(inventoryService.getInventoryById(id));
    }

    @PostMapping
    @Operation(summary = "新增物料")
    @RequiresPermission("inventory:inbound")
    public Result<Void> create(@Valid @RequestBody InventoryItemCreateRequest request) {
        inventoryService.createInventoryItem(request);
        return Result.success();
    }

    @PutMapping("/{id}")
    @Operation(summary = "修改物料")
    @RequiresPermission("inventory:inbound")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody InventoryItemUpdateRequest request) {
        inventoryService.updateInventoryItem(id, request);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除物料")
    @RequiresPermission("inventory:inbound")
    public Result<Void> delete(@PathVariable Long id) {
        inventoryService.deleteInventoryItem(id);
        return Result.success();
    }

    @PostMapping("/inbound")
    @Operation(summary = "入库操作")
    @RequiresPermission("inventory:inbound")
    public Result<Void> inbound(@Valid @RequestBody InventoryInboundRequest request) {
        inventoryService.inbound(request);
        return Result.success();
    }

    @PostMapping("/outbound")
    @Operation(summary = "出库操作")
    @RequiresPermission("inventory:outbound")
    public Result<Void> outbound(@Valid @RequestBody InventoryOutboundRequest request) {
        inventoryService.outbound(request);
        return Result.success();
    }

    @PostMapping("/check")
    @Operation(summary = "盘点操作")
    @RequiresPermission("inventory:inbound")
    public Result<Void> check(@Valid @RequestBody InventoryCheckRequest request) {
        inventoryService.stockCheck(request);
        return Result.success();
    }

    @GetMapping("/flow/list")
    @Operation(summary = "库存流水列表")
    @RequiresPermission("inventory:list")
    public Result<IPage<InventoryFlowResponse>> flowList(
            InventoryFlowQueryRequest query,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(inventoryService.getFlowList(query, pageNum, pageSize));
    }

    @GetMapping("/alert/list")
    @Operation(summary = "预警列表")
    @RequiresPermission("inventory:list")
    public Result<IPage<InventoryAlertResponse>> alertList(
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(inventoryService.getAlertList(status, pageNum, pageSize));
    }

    @PutMapping("/alert/{id}/handle")
    @Operation(summary = "处理预警")
    @RequiresPermission("inventory:inbound")
    public Result<Void> handleAlert(@PathVariable Long id) {
        inventoryService.handleAlert(id);
        return Result.success();
    }
}
