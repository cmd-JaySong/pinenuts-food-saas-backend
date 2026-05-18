package com.pinenuts.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.pinenuts.dto.dish.*;

public interface DishService {
    IPage<DishResponse> getDishList(Integer pageNum, Integer pageSize, DishQueryRequest query);
    DishResponse getDishById(Long id);
    void createDish(DishCreateRequest request);
    void updateDish(Long id, DishUpdateRequest request);
    void deleteDish(Long id);
    void updateDishStatus(Long id, Integer status);
    void batchUpdateStatus(BatchStatusRequest request);
}
