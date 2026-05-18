package com.pinenuts.dto.category;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 菜品分类响应DTO
 * 这是在第四周发现的BUG：使用@Builder注解时缺少@NoArgsConstructor，导致Redis反序列化失败
 * 修复方案：添加@NoArgsConstructor和@AllArgsConstructor注解，确保Redis序列化器能够正常反序列化对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryResponse {
    private Long id;
    private Long parentId;
    private String categoryName;
    private Integer sortOrder;
    private Integer status;
    private List<CategoryResponse> children;
}
