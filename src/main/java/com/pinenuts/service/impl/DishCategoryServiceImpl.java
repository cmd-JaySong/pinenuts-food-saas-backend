package com.pinenuts.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.pinenuts.common.ResultCode;
import com.pinenuts.common.exception.BusinessException;
import com.pinenuts.dto.category.CategoryCreateRequest;
import com.pinenuts.dto.category.CategoryResponse;
import com.pinenuts.dto.category.CategoryUpdateRequest;
import com.pinenuts.entity.Dish;
import com.pinenuts.entity.DishCategory;
import com.pinenuts.mapper.DishCategoryMapper;
import com.pinenuts.mapper.DishMapper;
import com.pinenuts.service.DishCategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DishCategoryServiceImpl implements DishCategoryService {

    private final DishCategoryMapper dishCategoryMapper;
    private final DishMapper dishMapper;

    @Override
    @Cacheable(value = "dishCategory", key = "'tree'")
    public List<CategoryResponse> getCategoryTree() {
        LambdaQueryWrapper<DishCategory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DishCategory::getStatus, 1);
        List<DishCategory> allCategories = dishCategoryMapper.selectList(wrapper);
        return buildTree(allCategories, 0L);
    }

    @Override
    public List<CategoryResponse> getCategoryList() {
        LambdaQueryWrapper<DishCategory> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(DishCategory::getSortOrder);
        List<DishCategory> allCategories = dishCategoryMapper.selectList(wrapper);
        return allCategories.stream().map(this::convertToResponse).collect(Collectors.toList());
    }

    @Override
    @CacheEvict(value = "dishCategory", allEntries = true)
    public void createCategory(CategoryCreateRequest request) {
        // 校验同父级同名重复
        LambdaQueryWrapper<DishCategory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DishCategory::getParentId, request.getParentId())
                .eq(DishCategory::getCategoryName, request.getCategoryName());
        if (dishCategoryMapper.selectCount(wrapper) > 0) {
            throw new BusinessException(ResultCode.CATEGORY_NAME_DUPLICATE);
        }

        DishCategory category = new DishCategory();
        category.setParentId(request.getParentId());
        category.setCategoryName(request.getCategoryName());
        category.setSortOrder(request.getSortOrder());
        category.setStatus(1);

        dishCategoryMapper.insert(category);
        log.info("新增菜品分类成功: {}", request.getCategoryName());
    }

    @Override
    @CacheEvict(value = "dishCategory", allEntries = true)
    public void updateCategory(Long id, CategoryUpdateRequest request) {
        DishCategory category = dishCategoryMapper.selectById(id);
        if (category == null) {
            throw new BusinessException(ResultCode.CATEGORY_NOT_FOUND);
        }

        // 校验同父级同名重复（排除自身）
        LambdaQueryWrapper<DishCategory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DishCategory::getParentId, category.getParentId())
                .eq(DishCategory::getCategoryName, request.getCategoryName())
                .ne(DishCategory::getId, id);
        if (dishCategoryMapper.selectCount(wrapper) > 0) {
            throw new BusinessException(ResultCode.CATEGORY_NAME_DUPLICATE);
        }

        category.setCategoryName(request.getCategoryName());
        if (request.getSortOrder() != null) {
            category.setSortOrder(request.getSortOrder());
        }
        if (request.getStatus() != null) {
            category.setStatus(request.getStatus());
        }

        dishCategoryMapper.updateById(category);
        log.info("更新菜品分类成功: id={}", id);
    }

    @Override
    @CacheEvict(value = "dishCategory", allEntries = true)
    public void deleteCategory(Long id) {
        DishCategory category = dishCategoryMapper.selectById(id);
        if (category == null) {
            throw new BusinessException(ResultCode.CATEGORY_NOT_FOUND);
        }

        // 校验是否有子分类
        LambdaQueryWrapper<DishCategory> childWrapper = new LambdaQueryWrapper<>();
        childWrapper.eq(DishCategory::getParentId, id);
        if (dishCategoryMapper.selectCount(childWrapper) > 0) {
            throw new BusinessException(ResultCode.CATEGORY_HAS_CHILDREN);
        }

        // 校验是否有菜品
        LambdaQueryWrapper<Dish> dishWrapper = new LambdaQueryWrapper<>();
        dishWrapper.eq(Dish::getCategoryId, id);
        if (dishMapper.selectCount(dishWrapper) > 0) {
            throw new BusinessException(ResultCode.CATEGORY_HAS_DISHES);
        }

        dishCategoryMapper.deleteById(id);
        log.info("删除菜品分类成功: id={}", id);
    }

    private List<CategoryResponse> buildTree(List<DishCategory> all, Long parentId) {
        return all.stream()
                .filter(c -> c.getParentId().equals(parentId))
                .sorted(Comparator.comparingInt(DishCategory::getSortOrder))
                .map(c -> CategoryResponse.builder()
                        .id(c.getId())
                        .parentId(c.getParentId())
                        .categoryName(c.getCategoryName())
                        .sortOrder(c.getSortOrder())
                        .status(c.getStatus())
                        .children(buildTree(all, c.getId()))
                        .build())
                .collect(Collectors.toList());
    }

    private CategoryResponse convertToResponse(DishCategory category) {
        return CategoryResponse.builder()
                .id(category.getId())
                .parentId(category.getParentId())
                .categoryName(category.getCategoryName())
                .sortOrder(category.getSortOrder())
                .status(category.getStatus())
                .children(null)
                .build();
    }
}
