package com.pinenuts.controller;

import com.pinenuts.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@Tag(name = "系统健康检查")
public class HealthController {

    @GetMapping("/health")
    @Operation(summary = "健康检查接口")
    public Result<String> health() {
        return Result.success("松籽餐饮数字化管理平台 - 服务运行中");
    }

}
