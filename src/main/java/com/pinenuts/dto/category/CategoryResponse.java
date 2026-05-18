package com.pinenuts.dto.category;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class CategoryResponse {
    private Long id;
    private Long parentId;
    private String categoryName;
    private Integer sortOrder;
    private Integer status;
    private List<CategoryResponse> children;
}
