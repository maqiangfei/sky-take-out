package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.annotation.AutoFill;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.enumeration.OperationType;
import com.sky.vo.DishItemVO;
import com.sky.vo.SetmealVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

/**
 * @author maqiangfei
 * @since 2024/10/2 下午4:57
 */
@Mapper
public interface SetmealMapper {

    /**
     * 查询指定分类及状态的套餐数量
     * @param categoryId
     * @return
     */
    Integer countByCategoryIdAndStatus(Long categoryId, Integer status);

    /**
     * 新增套餐
     * @param setmeal
     */
    @AutoFill(OperationType.INSERT)
    void save(Setmeal setmeal);

    /**
     * 分页查询套餐
     * @param setmealPageQueryDTO
     * @return
     */
    Page<SetmealVO> pageQuery(SetmealPageQueryDTO setmealPageQueryDTO);

    /**
     * 更新套餐信息
     * @param setmeal
     */
    @AutoFill(OperationType.UPDATE)
    Integer update(Setmeal setmeal);

    /**
     * 根据id查询套餐及其菜品
     * @return
     */
    SetmealVO getByIdWithSetmealDish(Long id);

    /**
     * 根据id查询分类id
     * @param id
     * @return
     */
    Long getCategoryId(Long id);

    /**
     * 批量删除套餐
     * @param ids
     */
    void deleteByIds(List<Long> ids);

    /**
     * 根据id查询图片路径
     * @param ids
     * @return
     */
    List<String> getImageByIds(List<Long> ids);

    /**
     * 批量查询起售套餐的数量
     * @param ids
     * @return
     */
    Integer countEnabledByIds(List<Long> ids);

    /**
     * 条件查询套餐
     * @param setmeal
     * @return
     */
    List<Setmeal> list(Setmeal setmeal);

    /**
     * 根据套餐id查询菜品选项
     * @param id
     * @return
     */
    List<DishItemVO> getDishItemBySetmealId(Long id);

    /**
     * 根据id查询套餐
     * @param setmealId
     * @return
     */
    Setmeal getById(Long setmealId);

    /**
     * 根据动态条件查询套餐数量
     * @param map
     * @return
     */
    Integer countByMap(Map<String, Integer> map);
}
