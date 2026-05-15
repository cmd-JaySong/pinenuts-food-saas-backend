package com.pinenuts.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.pinenuts.common.Result;
import com.pinenuts.common.annotation.RequiresPermission;
import com.pinenuts.dto.store.StoreCreateRequest;
import com.pinenuts.dto.store.StoreQueryRequest;
import com.pinenuts.dto.store.StoreResponse;
import com.pinenuts.dto.store.StoreUpdateRequest;
import com.pinenuts.service.StoreService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/store")
@RequiredArgsConstructor
@Tag(name = "门店管理", description = "门店CRUD接口")
public class StoreController {

    private final StoreService storeService;

    @GetMapping("/list")
    @Operation(summary = "门店列表（分页）")
    @RequiresPermission("store:list")
    public Result<IPage<StoreResponse>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            StoreQueryRequest query) {
        return Result.success(storeService.getStoreList(pageNum, pageSize, query));
    }

    @GetMapping("/all")
    @Operation(summary = "所有门店（下拉用）")
    @RequiresPermission("store:list")
    public Result<List<StoreResponse>> all() {
        return Result.success(storeService.getAllStores());
    }

    @GetMapping("/{id}")
    @Operation(summary = "门店详情")
    @RequiresPermission("store:list")
    public Result<StoreResponse> getById(@PathVariable Long id) {
        return Result.success(storeService.getStoreById(id));
    }

    @PostMapping
    @Operation(summary = "新增门店")
    @RequiresPermission("store:create")
    public Result<Void> create(@Valid @RequestBody StoreCreateRequest request) {
        storeService.createStore(request);
        return Result.success();
    }

    @PutMapping("/{id}")
    @Operation(summary = "修改门店")
    @RequiresPermission("store:update")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody StoreUpdateRequest request) {
        storeService.updateStore(id, request);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除门店")
    @RequiresPermission("store:delete")
    public Result<Void> delete(@PathVariable Long id) {
        storeService.deleteStore(id);
        return Result.success();
    }
}
