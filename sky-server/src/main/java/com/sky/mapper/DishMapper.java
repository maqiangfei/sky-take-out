package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.annotation.AutoFill;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.enumeration.OperationType;
import com.sky.vo.DishVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @author maqiangfei
 * @since 2024/10/2 下午4:57
 */
@Mapper
public interface DishMapper {

    /**
     * 查询指定分类和状态的菜品数量
     * @param categoryId
     * @return
     */
    Integer countByCategoryIdAndStatus(Long categoryId, Integer status);

    /**
     * 插入菜品数据
     * @param dish
     */
    @AutoFill(OperationType.INSERT)
    void insert(Dish dish);

    /**
     * 菜品分页查询
     * @param dishPageQueryDTO
     * @return
     */
    Page<DishVO> pageQuery(DishPageQueryDTO dishPageQueryDTO);

    /**
     * 查询起售的个数
     * @param ids
     * @return
     */
    int countByStatusByIds(List<Long> ids);

    /**
     * 批量删除菜品
     * @param ids
     */
    void deleteBatch(List<Long> ids);

    /**
     * 批量查询菜品图片
     * @param ids
     * @return
     */
    List<String> getImageByIds(List<Long> ids);

    /**
     * 根据id查询菜品
     * @return
     */
    DishVO getByIdWithFlavor(Long id);

    /**
     * 修改菜品
     * @param dish
     */
    @AutoFill(OperationType.UPDATE)
    Integer update(Dish dish);

    /**
     * 菜品分类查询
     * @param categoryId
     * @return
     */
    List<Dish> listByType(Long categoryId);

    /**
     * 查询菜品分类id
     * @param id
     * @return
     */
    Long getCategoryId(Long id);

    /**
     * 动态条件查询菜品和口味
     * @param dish
     * @return
     */
    List<DishVO> listWithFlavor(Dish dish);
}
