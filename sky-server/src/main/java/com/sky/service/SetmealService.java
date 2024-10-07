package com.sky.service;

import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.result.PageResult;
import com.sky.vo.DishItemVO;
import com.sky.vo.SetmealVO;

import java.util.List;

/**
 * @author maqiangfei
 * @since 2024/10/4 下午1:15
 */
public interface SetmealService {

    /**
     * 新增套餐及与菜品的关系
     * @param setmealDTO
     */
    void saveWithSetmealDish(SetmealDTO setmealDTO);

    /**
     * 分页查询套餐
     * @param setmealPageQueryDTO
     * @return
     */
    PageResult pageQuery(SetmealPageQueryDTO setmealPageQueryDTO);

    /**
     * 修改套餐
     * @param setmealDTO
     */
    void updateWithSetmealDish(SetmealDTO setmealDTO);

    /**
     * 根据id查询套餐
     * @param id
     * @return
     */
    SetmealVO getByIdWithSetmealDish(Long id);

    /**
     * 启售停售套餐
     * @param status
     * @param id
     */
    void startOrStop(Integer status, Long id);

    /**
     * 根据id删除套餐及和菜品的关系
     * @param id
     */
    void deleteBatchWithSetmealDish(List<Long> id);

    /**
     * 条件查询
     * @param setmeal
     * @return
     */
    List<Setmeal> list(Setmeal setmeal);

    /**
     * 根据id查询菜品选项
     * @param id
     * @return
     */
    List<DishItemVO> getDishItemById(Long id);
}
