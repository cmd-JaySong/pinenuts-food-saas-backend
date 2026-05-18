package com.pinenuts.service.impl;

import com.pinenuts.common.ResultCode;
import com.pinenuts.common.exception.BusinessException;
import com.pinenuts.service.FileUploadService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class FileUploadServiceImpl implements FileUploadService {

    @Value("${file.upload.path}")
    private String uploadPath;

    @Value("${file.upload.url-prefix}")
    private String urlPrefix;

    @Value("${file.upload.allowed-types}")
    private String allowedTypes;

    @Override
    public String uploadFile(MultipartFile file) {
        // 1. 校验文件类型
        String contentType = file.getContentType();
        List<String> allowedTypeList = Arrays.asList(allowedTypes.split(","));
        if (contentType == null || !allowedTypeList.contains(contentType)) {
            throw new BusinessException(ResultCode.FILE_TYPE_NOT_ALLOWED);
        }

        // 2. 生成存储路径（按日期分目录）
        String dateDir = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String dir = uploadPath + File.separator + dateDir;
        File dirFile = new File(dir);
        if (!dirFile.exists()) {
            dirFile.mkdirs();
        }

        // 3. 生成唯一文件名
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String newFilename = UUID.randomUUID().toString().replace("-", "") + extension;

        // 4. 保存文件
        File destFile = new File(dir + File.separator + newFilename);
        try {
            file.transferTo(destFile);
        } catch (IOException e) {
            log.error("文件上传失败: {}", e.getMessage(), e);
            throw new BusinessException(ResultCode.FILE_UPLOAD_FAILED);
        }

        // 5. 返回访问URL
        return urlPrefix + "/" + dateDir + "/" + newFilename;
    }
}
