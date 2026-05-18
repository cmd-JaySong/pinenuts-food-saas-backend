package com.pinenuts.dto.dish;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class BatchStatusRequest {
    @NotEmpty(message = "菜品ID列表不能为空")
    private List<Long> ids;

    @NotNull(message = "状态不能为空")
    private Integer status;
}
