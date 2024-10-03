package com.sky.mapper;

import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @author maqiangfei
 * @since 2024/10/3 下午6:26
 */
@Mapper
public interface SetmealDishMapper {

    /**
     * 查询和菜品关联的套餐数量
     * @param ids
     * @return
     */
    int countByDishIds(List<Long> ids);
}
