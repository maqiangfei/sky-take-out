package com.sky.mapper;

import org.apache.ibatis.annotations.Mapper;

/**
 * @author maqiangfei
 * @since 2024/10/2 下午4:57
 */
@Mapper
public interface DishMapper {

    /**
     * 根据分类查询菜品数量
     * @param categoryId
     * @return
     */
    Integer countByCategoryId(Long categoryId);
}
