package com.pinenuts.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pinenuts.entity.InventoryItem;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface InventoryItemMapper extends BaseMapper<InventoryItem> {
}
