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
     * 根据分类查询菜品数量
     * @param categoryId
     * @return
     */
    Integer countByCategoryId(Long categoryId);

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
     * 查询启用的个数
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
}
