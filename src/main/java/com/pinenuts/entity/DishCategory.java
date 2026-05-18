package com.pinenuts.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_dish_category")
public class DishCategory {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long tenantId;
    private Long parentId;
    private String categoryName;
    private Integer sortOrder;
    private Integer status;
    @TableLogic
    private Integer deleted;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
