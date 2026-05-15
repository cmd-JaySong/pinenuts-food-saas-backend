package com.pinenuts.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName("sys_permission")
public class SysPermission {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long parentId;
    private String permissionCode;
    private String permissionName;
    private Integer type;
    private String path;
    private String icon;
    private Integer sortOrder;
    private Integer status;

    @TableLogic
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /** 子权限列表（非数据库字段） */
    @TableField(exist = false)
    private List<SysPermission> children;
}
