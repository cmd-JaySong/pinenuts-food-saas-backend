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
    PERMISSION_DENIED(403, "权限不足"),

    STORE_NOT_FOUND(404, "门店不存在"),
    STORE_CODE_DUPLICATE(400, "门店编号已存在"),
    STAFF_NOT_FOUND(404, "员工不存在"),

    DISH_NOT_FOUND(404, "菜品不存在"),
    DISH_CODE_DUPLICATE(400, "菜品编号已存在"),
    CATEGORY_NOT_FOUND(404, "分类不存在"),
    CATEGORY_NAME_DUPLICATE(400, "分类名称已存在"),
    CATEGORY_HAS_CHILDREN(400, "该分类下存在子分类，无法删除"),
    CATEGORY_HAS_DISHES(400, "该分类下存在菜品，无法删除"),
    FILE_UPLOAD_FAILED(500, "文件上传失败"),
    FILE_TYPE_NOT_ALLOWED(400, "不支持的文件类型"),

    // 库存相关 5xxx
    INVENTORY_NOT_FOUND(5001, "库存物料不存在"),
    INVENTORY_INSUFFICIENT(5002, "库存不足，无法出库"),
    INVENTORY_CODE_DUPLICATE(5003, "物料编码已存在"),
    INVENTORY_CANNOT_DELETE(5004, "库存量不为零，无法删除"),
    INVENTORY_ALERT_NOT_FOUND(5005, "预警记录不存在"),

    // 采购相关 6xxx
    PURCHASE_NOT_FOUND(6001, "采购单不存在"),
    PURCHASE_CODE_DUPLICATE(6002, "采购单号已存在"),
    PURCHASE_INVALID_STATUS(6003, "当前状态不允许此操作"),
    PURCHASE_ITEMS_EMPTY(6004, "采购明细不能为空"),
    PURCHASE_APPROVAL_NOT_FOUND(6005, "审批记录不存在"),
    PURCHASE_NO_PERMISSION(6006, "无权执行此审批操作");

    private final int code;
    private final String message;

}
