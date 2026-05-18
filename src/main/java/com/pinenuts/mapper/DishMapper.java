package com.pinenuts.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pinenuts.entity.Dish;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DishMapper extends BaseMapper<Dish> {
}
