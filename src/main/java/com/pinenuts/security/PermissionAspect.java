package com.pinenuts.security;

import com.pinenuts.common.ResultCode;
import com.pinenuts.common.annotation.RequiresPermission;
import com.pinenuts.common.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class PermissionAspect {

    @Around("@annotation(requiresPermission)")
    public Object checkPermission(ProceedingJoinPoint joinPoint, RequiresPermission requiresPermission) throws Throwable {
        String requiredPermission = requiresPermission.value();

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof LoginUser loginUser)) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }

        // 超级管理员跳过权限检查
        if (loginUser.getRoles().contains("SUPER_ADMIN")) {
            return joinPoint.proceed();
        }

        // 检查是否拥有所需权限
        if (!loginUser.getPermissions().contains(requiredPermission)) {
            log.warn("用户 {} 权限不足，需要权限: {}", loginUser.getUsername(), requiredPermission);
            throw new BusinessException(ResultCode.PERMISSION_DENIED);
        }

        return joinPoint.proceed();
    }
}
