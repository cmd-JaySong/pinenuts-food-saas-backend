package com.pinenuts.controller;

import com.pinenuts.common.Result;
import com.pinenuts.dto.*;
import com.pinenuts.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "认证管理", description = "登录/登出/刷新Token/获取用户信息")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "使用用户名和密码登录，返回 Access Token 和 Refresh Token")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return Result.success(response);
    }

    @PostMapping("/refresh")
    @Operation(summary = "刷新 Token", description = "使用 Refresh Token 获取新的 Access Token")
    public Result<LoginResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        LoginResponse response = authService.refreshToken(request.getRefreshToken());
        return Result.success(response);
    }

    @PostMapping("/logout")
    @Operation(summary = "退出登录", description = "清除服务端 Refresh Token")
    public Result<Void> logout() {
        authService.logout();
        return Result.success();
    }

    @GetMapping("/userinfo")
    @Operation(summary = "获取当前用户信息", description = "获取当前登录用户的信息、角色和权限列表")
    public Result<UserInfoResponse> getUserInfo() {
        UserInfoResponse userInfo = authService.getCurrentUserInfo();
        return Result.success(userInfo);
    }
}
