package com.sky.mapper;

import com.sky.entity.DishFlavor;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @author maqiangfei
 * @since 2024/10/3 下午4:05
 */
@Mapper
public interface DishFlavorMapper {

    /**
     * 批量插入口味
     * @param flavors
     */
    void insertBatch(List<DishFlavor> flavors);

    /**
     * 批量删除菜品相关口味
     * @param dishIds
     */
    void deleteBatchByDishIds(List<Long> dishIds);

    /**
     * 删除菜品相关口味
     * @param dishId
     */
    void deleteByDishId(Long dishId);

    /**
     * 批量插入口味
     * @param flavors
     */
    void saveBatch(List<DishFlavor> flavors);
}
