package com.pinenuts.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pinenuts.common.ResultCode;
import com.pinenuts.common.exception.BusinessException;
import com.pinenuts.dto.dish.*;
import com.pinenuts.entity.Dish;
import com.pinenuts.entity.DishCategory;
import com.pinenuts.mapper.DishCategoryMapper;
import com.pinenuts.mapper.DishMapper;
import com.pinenuts.service.DishService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class DishServiceImpl implements DishService {

    private final DishMapper dishMapper;
    private final DishCategoryMapper dishCategoryMapper;

    @Override
    public IPage<DishResponse> getDishList(Integer pageNum, Integer pageSize, DishQueryRequest query) {
        LambdaQueryWrapper<Dish> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(query.getDishName()), Dish::getDishName, query.getDishName())
                .eq(query.getCategoryId() != null, Dish::getCategoryId, query.getCategoryId())
                .eq(query.getStatus() != null, Dish::getStatus, query.getStatus())
                .orderByDesc(Dish::getCreatedAt);

        Page<Dish> page = dishMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);

        return page.convert(this::convertToResponse);
    }

    @Override
    public DishResponse getDishById(Long id) {
        Dish dish = dishMapper.selectById(id);
        if (dish == null) {
            throw new BusinessException(ResultCode.DISH_NOT_FOUND);
        }
        return convertToResponse(dish);
    }

    @Override
    @CacheEvict(value = "dish", allEntries = true)
    public void createDish(DishCreateRequest request) {
        // 校验分类是否存在
        DishCategory category = dishCategoryMapper.selectById(request.getCategoryId());
        if (category == null) {
            throw new BusinessException(ResultCode.CATEGORY_NOT_FOUND);
        }

        // 若有 dishCode 则校验唯一
        if (StringUtils.hasText(request.getDishCode())) {
            LambdaQueryWrapper<Dish> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Dish::getDishCode, request.getDishCode());
            if (dishMapper.selectCount(wrapper) > 0) {
                throw new BusinessException(ResultCode.DISH_CODE_DUPLICATE);
            }
        }

        Dish dish = new Dish();
        dish.setCategoryId(request.getCategoryId());
        dish.setDishName(request.getDishName());
        dish.setDishCode(request.getDishCode());
        dish.setPrice(request.getPrice());
        dish.setImageUrl(request.getImageUrl());
        dish.setDescription(request.getDescription());
        dish.setSpecifications(request.getSpecifications());
        dish.setStatus(1);

        dishMapper.insert(dish);
        log.info("新增菜品成功: {}", request.getDishName());
    }

    @Override
    @CacheEvict(value = "dish", allEntries = true)
    public void updateDish(Long id, DishUpdateRequest request) {
        Dish dish = dishMapper.selectById(id);
        if (dish == null) {
            throw new BusinessException(ResultCode.DISH_NOT_FOUND);
        }

        // 若修改了 dishCode 则校验唯一
        if (StringUtils.hasText(request.getDishCode()) && !request.getDishCode().equals(dish.getDishCode())) {
            LambdaQueryWrapper<Dish> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Dish::getDishCode, request.getDishCode())
                    .ne(Dish::getId, id);
            if (dishMapper.selectCount(wrapper) > 0) {
                throw new BusinessException(ResultCode.DISH_CODE_DUPLICATE);
            }
        }

        if (request.getCategoryId() != null) {
            dish.setCategoryId(request.getCategoryId());
        }
        dish.setDishName(request.getDishName());
        dish.setDishCode(request.getDishCode());
        dish.setPrice(request.getPrice());
        dish.setImageUrl(request.getImageUrl());
        dish.setDescription(request.getDescription());
        dish.setSpecifications(request.getSpecifications());
        if (request.getStatus() != null) {
            dish.setStatus(request.getStatus());
        }

        dishMapper.updateById(dish);
        log.info("更新菜品成功: id={}", id);
    }

    @Override
    @CacheEvict(value = "dish", allEntries = true)
    public void deleteDish(Long id) {
        Dish dish = dishMapper.selectById(id);
        if (dish == null) {
            throw new BusinessException(ResultCode.DISH_NOT_FOUND);
        }
        dishMapper.deleteById(id);
        log.info("删除菜品成功: id={}", id);
    }

    @Override
    @CacheEvict(value = "dish", allEntries = true)
    public void updateDishStatus(Long id, Integer status) {
        Dish dish = dishMapper.selectById(id);
        if (dish == null) {
            throw new BusinessException(ResultCode.DISH_NOT_FOUND);
        }
        dish.setStatus(status);
        dishMapper.updateById(dish);
        log.info("菜品状态更新成功: id={}, status={}", id, status);
    }

    @Override
    @CacheEvict(value = "dish", allEntries = true)
    public void batchUpdateStatus(BatchStatusRequest request) {
        LambdaUpdateWrapper<Dish> wrapper = new LambdaUpdateWrapper<>();
        wrapper.in(Dish::getId, request.getIds())
                .set(Dish::getStatus, request.getStatus());
        dishMapper.update(null, wrapper);
        log.info("批量更新菜品状态成功: ids={}, status={}", request.getIds(), request.getStatus());
    }

    private DishResponse convertToResponse(Dish dish) {
        String categoryName = "";
        DishCategory category = dishCategoryMapper.selectById(dish.getCategoryId());
        if (category != null) {
            categoryName = category.getCategoryName();
        }

        return DishResponse.builder()
                .id(dish.getId())
                .categoryId(dish.getCategoryId())
                .categoryName(categoryName)
                .dishName(dish.getDishName())
                .dishCode(dish.getDishCode())
                .price(dish.getPrice())
                .imageUrl(dish.getImageUrl())
                .description(dish.getDescription())
                .specifications(dish.getSpecifications())
                .status(dish.getStatus())
                .createdAt(dish.getCreatedAt())
                .updatedAt(dish.getUpdatedAt())
                .build();
    }
}
