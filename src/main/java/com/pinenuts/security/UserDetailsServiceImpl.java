package com.pinenuts.security;

import com.pinenuts.entity.SysPermission;
import com.pinenuts.entity.SysRole;
import com.pinenuts.entity.SysUser;
import com.pinenuts.mapper.SysPermissionMapper;
import com.pinenuts.mapper.SysRoleMapper;
import com.pinenuts.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final SysUserMapper userMapper;
    private final SysRoleMapper roleMapper;
    private final SysPermissionMapper permissionMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 查询用户
        SysUser user = userMapper.selectByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("用户不存在: " + username);
        }

        // 查询角色
        List<SysRole> roleEntities = roleMapper.selectRolesByUserId(user.getId());
        List<String> roles = roleEntities.stream()
                .map(SysRole::getRoleCode)
                .collect(Collectors.toList());

        // 查询权限
        List<SysPermission> permissionEntities = permissionMapper.selectPermissionsByUserId(user.getId());
        Set<String> permissions = permissionEntities.stream()
                .map(SysPermission::getPermissionCode)
                .collect(Collectors.toSet());

        return new LoginUser(user, roles, permissions);
    }
}
