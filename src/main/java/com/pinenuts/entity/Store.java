package com.pinenuts.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_store")
public class Store {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long tenantId;
    private String storeCode;
    private String storeName;
    private String address;
    private String contactPhone;
    private String businessHours;
    private Integer status;

    @TableLogic
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
