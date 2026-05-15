package com.pinenuts.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ResultCode {

    SUCCESS(200, "操作成功"),
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未授权"),
    FORBIDDEN(403, "禁止访问"),
    NOT_FOUND(404, "资源不存在"),
    INTERNAL_ERROR(500, "服务器内部错误"),

    LOGIN_FAILED(401, "用户名或密码错误"),
    TOKEN_INVALID(401, "Token 无效"),
    TOKEN_EXPIRED(401, "Token 已过期"),
    ACCOUNT_DISABLED(403, "账号已被禁用"),
    PERMISSION_DENIED(403, "权限不足");

    private final int code;
    private final String message;

}
