package com.pinenuts.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("t_dish")
public class Dish {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long tenantId;
    private Long categoryId;
    private String dishName;
    private String dishCode;
    private BigDecimal price;
    private String imageUrl;
    private String description;
    private String specifications;
    private Integer status;
    @TableLogic
    private Integer deleted;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
