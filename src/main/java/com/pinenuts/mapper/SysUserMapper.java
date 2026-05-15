package com.pinenuts.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pinenuts.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {
    /**
     * 根据用户名查询用户（包含角色信息）
     */
    SysUser selectByUsername(@Param("username") String username);
}
