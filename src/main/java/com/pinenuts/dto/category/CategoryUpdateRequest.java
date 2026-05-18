package com.pinenuts.dto.category;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CategoryUpdateRequest {
    @NotBlank(message = "分类名称不能为空")
    @Size(max = 50)
    private String categoryName;
    private Integer sortOrder;
    private Integer status;
}
