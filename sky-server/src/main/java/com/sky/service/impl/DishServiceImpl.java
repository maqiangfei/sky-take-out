package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.utils.FastDfsUtil;
import com.sky.vo.DishVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author maqiangfei
 * @since 2024/10/3 上午9:24
 */
@Service
@Slf4j
public class DishServiceImpl implements DishService {

    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private DishFlavorMapper dishFlavorMapper;

    @Autowired
    private SetmealDishMapper setmealDishMapper;

    @Autowired
    private FastDfsUtil fastDfsUtil;

    /**
     * 新增菜品和对应口味
     * @param dishDTO
     */
    @Override
    @Transactional
    public void saveWithFlavor(DishDTO dishDTO) {
        // 向菜品表插入数据
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);
        dishMapper.insert(dish);

        // 获取插入菜品的主键
        Long dishId = dish.getId();

        // 向口味表插入n条数据
        List<DishFlavor> flavors = dishDTO.getFlavors();
        if (flavors != null && flavors.size() > 0) {
            flavors.forEach(flavor -> flavor.setDishId(dishId));
            dishFlavorMapper.insertBatch(flavors);
        }
    }

    /**
     * 菜品分页查询
     * @param dishPageQueryDTO
     * @return
     */
    @Override
    public PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO) {
        PageHelper.startPage(dishPageQueryDTO.getPage(), dishPageQueryDTO.getPageSize());
        Page<DishVO> page = dishMapper.pageQuery(dishPageQueryDTO);
        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 批量删除菜品
     * @param ids
     */
    @Override
    @Transactional
    public void deleteBatchWithFlavor(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        // 查询是否有菜品起售
        int count = dishMapper.countByStatusByIds(ids);
        if (count > 0) {
            throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
        }
        // 查询是否有关联的菜品
        count = setmealDishMapper.countByDishIds(ids);
        if (count > 0) {
            throw new DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
        }
        // 查询菜品的image
        List<String> images = dishMapper.getImageByIds(ids);
        // 删除图片
        images.forEach(image -> {
            image = image.substring(image.indexOf("/", 8) + 1);
            if (fastDfsUtil.delete(image) > 0) {
                log.error("清除图片失败，{}", image);
            }
        });
        // 删除菜品相关口味
        dishFlavorMapper.deleteBatch(ids);
        // 删除菜品
        dishMapper.deleteBatch(ids);
    }
}
