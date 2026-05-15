package com.pinenuts.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pinenuts.entity.SysPermission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface SysPermissionMapper extends BaseMapper<SysPermission> {
    /**
     * 根据用户ID查询权限列表
     */
    List<SysPermission> selectPermissionsByUserId(@Param("userId") Long userId);

    /**
     * 根据用户ID查询菜单树（type=1的权限）
     */
    List<SysPermission> selectMenusByUserId(@Param("userId") Long userId);
}
