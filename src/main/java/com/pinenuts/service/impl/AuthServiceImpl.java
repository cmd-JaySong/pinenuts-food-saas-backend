package com.pinenuts.service.impl;

import com.pinenuts.common.ResultCode;
import com.pinenuts.common.exception.BusinessException;
import com.pinenuts.dto.*;
import com.pinenuts.entity.SysPermission;
import com.pinenuts.entity.SysUser;
import com.pinenuts.mapper.SysPermissionMapper;
import com.pinenuts.mapper.SysRoleMapper;
import com.pinenuts.mapper.SysUserMapper;
import com.pinenuts.security.JwtUtils;
import com.pinenuts.security.LoginUser;
import com.pinenuts.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final SysUserMapper userMapper;
    private final SysRoleMapper roleMapper;
    private final SysPermissionMapper permissionMapper;
    private final JwtUtils jwtUtils;
    private final PasswordEncoder passwordEncoder;
    private final StringRedisTemplate redisTemplate;

    private static final String REFRESH_TOKEN_PREFIX = "auth:refresh_token:";

    @Override
    public LoginResponse login(LoginRequest request) {
        // 1. 查询用户
        SysUser user = userMapper.selectByUsername(request.getUsername());
        if (user == null) {
            throw new BusinessException(ResultCode.LOGIN_FAILED);
        }

        // 2. 检查账号状态
        if (user.getStatus() == 0) {
            throw new BusinessException(ResultCode.ACCOUNT_DISABLED);
        }

        // 3. 验证密码
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(ResultCode.LOGIN_FAILED);
        }

        // 4. 生成 Token
        String accessToken = jwtUtils.generateAccessToken(user.getId(), user.getUsername(), user.getTenantId());
        String refreshToken = jwtUtils.generateRefreshToken(user.getId());

        // 5. Refresh Token 存入 Redis
        redisTemplate.opsForValue().set(
                REFRESH_TOKEN_PREFIX + user.getId(),
                refreshToken,
                jwtUtils.getRefreshTokenExpiration(),
                TimeUnit.MILLISECONDS
        );

        log.info("用户登录成功: {}", user.getUsername());

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(jwtUtils.getAccessTokenExpiration())
                .build();
    }

    @Override
    public LoginResponse refreshToken(String refreshToken) {
        // 1. 验证 Refresh Token
        if (!jwtUtils.validateToken(refreshToken)) {
            throw new BusinessException(ResultCode.TOKEN_INVALID);
        }

        // 2. 获取用户ID
        Long userId = jwtUtils.getUserIdFromToken(refreshToken);

        // 3. 检查 Redis 中是否存在
        String storedToken = redisTemplate.opsForValue().get(REFRESH_TOKEN_PREFIX + userId);
        if (storedToken == null || !storedToken.equals(refreshToken)) {
            throw new BusinessException(ResultCode.TOKEN_INVALID);
        }

        // 4. 查询用户信息
        SysUser user = userMapper.selectById(userId);
        if (user == null || user.getStatus() == 0) {
            throw new BusinessException(ResultCode.ACCOUNT_DISABLED);
        }

        // 5. 生成新的 Access Token
        String newAccessToken = jwtUtils.generateAccessToken(user.getId(), user.getUsername(), user.getTenantId());

        // 6. 生成新的 Refresh Token（旋转刷新）
        String newRefreshToken = jwtUtils.generateRefreshToken(user.getId());
        redisTemplate.opsForValue().set(
                REFRESH_TOKEN_PREFIX + userId,
                newRefreshToken,
                jwtUtils.getRefreshTokenExpiration(),
                TimeUnit.MILLISECONDS
        );

        return LoginResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .expiresIn(jwtUtils.getAccessTokenExpiration())
                .build();
    }

    @Override
    public void logout() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof LoginUser loginUser) {
            Long userId = loginUser.getUser().getId();
            // 删除 Redis 中的 Refresh Token
            redisTemplate.delete(REFRESH_TOKEN_PREFIX + userId);
            log.info("用户登出成功: {}", loginUser.getUsername());
        }
    }

    @Override
    public UserInfoResponse getCurrentUserInfo() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof LoginUser loginUser)) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }

        SysUser user = loginUser.getUser();

        // 查询菜单树
        List<SysPermission> allMenus = permissionMapper.selectMenusByUserId(user.getId());
        List<SysPermission> menuTree = buildMenuTree(allMenus);

        return UserInfoResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .roles(loginUser.getRoles())
                .permissions(loginUser.getPermissions())
                .menus(menuTree)
                .build();
    }

    /**
     * 构建菜单树
     */
    private List<SysPermission> buildMenuTree(List<SysPermission> menus) {
        Map<Long, SysPermission> menuMap = new HashMap<>();
        List<SysPermission> rootMenus = new ArrayList<>();

        // 先将所有菜单放入 Map
        for (SysPermission menu : menus) {
            menu.setChildren(new ArrayList<>());
            menuMap.put(menu.getId(), menu);
        }

        // 构建树
        for (SysPermission menu : menus) {
            if (menu.getParentId() == 0) {
                rootMenus.add(menu);
            } else {
                SysPermission parent = menuMap.get(menu.getParentId());
                if (parent != null) {
                    parent.getChildren().add(menu);
                }
            }
        }

        return rootMenus;
    }
}
