package com.sky.mapper;

import com.sky.entity.SetmealDish;
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

    /**
     * 批量添加套餐和菜品关联数据
     * @param setmealDishes
     */
    void saveBatch(List<SetmealDish> setmealDishes);

    /**
     * 批量删除套餐与菜品的关联
     * @param setmealIds
     */
    void deleteBySetmealIds(List<Long> setmealIds);
}
