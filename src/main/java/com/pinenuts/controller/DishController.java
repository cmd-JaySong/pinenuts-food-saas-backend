package com.pinenuts.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.pinenuts.common.Result;
import com.pinenuts.common.annotation.RequiresPermission;
import com.pinenuts.dto.dish.*;
import com.pinenuts.service.DishService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dish")
@RequiredArgsConstructor
@Tag(name = "菜品管理", description = "菜品CRUD接口")
public class DishController {

    private final DishService dishService;

    @GetMapping("/list")
    @Operation(summary = "菜品列表（分页）")
    @RequiresPermission("dish:list")
    public Result<IPage<DishResponse>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            DishQueryRequest query) {
        return Result.success(dishService.getDishList(pageNum, pageSize, query));
    }

    @GetMapping("/{id}")
    @Operation(summary = "菜品详情")
    @RequiresPermission("dish:list")
    public Result<DishResponse> getById(@PathVariable Long id) {
        return Result.success(dishService.getDishById(id));
    }

    @PostMapping
    @Operation(summary = "新增菜品")
    @RequiresPermission("dish:create")
    public Result<Void> create(@Valid @RequestBody DishCreateRequest request) {
        dishService.createDish(request);
        return Result.success();
    }

    @PutMapping("/{id}")
    @Operation(summary = "修改菜品")
    @RequiresPermission("dish:update")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody DishUpdateRequest request) {
        dishService.updateDish(id, request);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除菜品")
    @RequiresPermission("dish:delete")
    public Result<Void> delete(@PathVariable Long id) {
        dishService.deleteDish(id);
        return Result.success();
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "菜品上下架")
    @RequiresPermission("dish:update")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        dishService.updateDishStatus(id, status);
        return Result.success();
    }

    @PutMapping("/batch-status")
    @Operation(summary = "批量上下架")
    @RequiresPermission("dish:update")
    public Result<Void> batchUpdateStatus(@Valid @RequestBody BatchStatusRequest request) {
        dishService.batchUpdateStatus(request);
        return Result.success();
    }
}
