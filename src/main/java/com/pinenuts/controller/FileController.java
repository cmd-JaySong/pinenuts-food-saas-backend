package com.pinenuts.controller;

import com.pinenuts.common.Result;
import com.pinenuts.service.FileUploadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/file")
@RequiredArgsConstructor
@Tag(name = "文件管理", description = "文件上传接口")
public class FileController {

    private final FileUploadService fileUploadService;

    @PostMapping("/upload")
    @Operation(summary = "上传文件")
    public Result<String> upload(@RequestParam("file") MultipartFile file) {
        String url = fileUploadService.uploadFile(file);
        return Result.success(url);
    }
}
