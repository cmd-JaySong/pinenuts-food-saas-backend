package com.pinenuts.dto.category;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CategoryCreateRequest {
    private Long parentId = 0L;

    @NotBlank(message = "分类名称不能为空")
    @Size(max = 50, message = "分类名称长度不超过50")
    private String categoryName;

    private Integer sortOrder = 0;
}
