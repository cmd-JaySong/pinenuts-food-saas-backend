package com.pinenuts.controller;

import com.pinenuts.common.Result;
import com.pinenuts.common.annotation.RequiresPermission;
import com.pinenuts.dto.category.CategoryCreateRequest;
import com.pinenuts.dto.category.CategoryResponse;
import com.pinenuts.dto.category.CategoryUpdateRequest;
import com.pinenuts.service.DishCategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dish-category")
@RequiredArgsConstructor
@Tag(name = "菜品分类管理", description = "菜品分类CRUD接口")
public class DishCategoryController {

    private final DishCategoryService dishCategoryService;

    @GetMapping("/tree")
    @Operation(summary = "获取分类树")
    @RequiresPermission("dish:list")
    public Result<List<CategoryResponse>> tree() {
        return Result.success(dishCategoryService.getCategoryTree());
    }

    @GetMapping("/list")
    @Operation(summary = "获取分类平铺列表")
    @RequiresPermission("dish:list")
    public Result<List<CategoryResponse>> list() {
        return Result.success(dishCategoryService.getCategoryList());
    }

    @PostMapping
    @Operation(summary = "新增分类")
    @RequiresPermission("dish:create")
    public Result<Void> create(@Valid @RequestBody CategoryCreateRequest request) {
        dishCategoryService.createCategory(request);
        return Result.success();
    }

    @PutMapping("/{id}")
    @Operation(summary = "修改分类")
    @RequiresPermission("dish:update")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody CategoryUpdateRequest request) {
        dishCategoryService.updateCategory(id, request);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除分类")
    @RequiresPermission("dish:delete")
    public Result<Void> delete(@PathVariable Long id) {
        dishCategoryService.deleteCategory(id);
        return Result.success();
    }
}
