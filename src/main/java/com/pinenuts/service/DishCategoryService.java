package com.pinenuts.service;

import com.pinenuts.dto.category.CategoryCreateRequest;
import com.pinenuts.dto.category.CategoryResponse;
import com.pinenuts.dto.category.CategoryUpdateRequest;

import java.util.List;

public interface DishCategoryService {
    List<CategoryResponse> getCategoryTree();
    List<CategoryResponse> getCategoryList();
    void createCategory(CategoryCreateRequest request);
    void updateCategory(Long id, CategoryUpdateRequest request);
    void deleteCategory(Long id);
}
