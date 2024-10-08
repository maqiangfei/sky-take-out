package com.sky.service.impl;

import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.LogConstant;
import com.sky.constant.MessageConstant;
import com.sky.constant.RedisConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.exception.EnableNotAllowedException;
import com.sky.exception.UpdateNotAllowedException;
import com.sky.mapper.CategoryMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.utils.FastDfsUtil;
import com.sky.vo.DishVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.TimeUnit;

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

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

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
        if (flavors != null && !flavors.isEmpty()) {
            flavors.forEach(flavor -> flavor.setDishId(dishId));
            dishFlavorMapper.insertBatch(flavors);
        }
        // 新增菜品默认未起售，所以不需要删除缓存
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
        // 查询是否有关联的套餐
        count = setmealDishMapper.countByDishIds(ids);
        if (count > 0) {
            throw new DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
        }
        // 查询菜品的image
        List<String> images = dishMapper.getImageByIds(ids);
        // 删除图片
        images.forEach(image -> {
            String fileId = image.substring(image.indexOf("/", 8) + 1);
            if (fastDfsUtil.delete(fileId) > 0)
                log.error(LogConstant.CLEAR_DISH_PIC_ERROR + ",{}", fileId);
        });
        // 删除菜品相关口味
        dishFlavorMapper.deleteBatchByDishIds(ids);
        // 删除菜品
        dishMapper.deleteBatch(ids);
        // 删除的菜品都是停售的，缓存已经被删除
    }

    /**
     *
     * @return
     */
    @Override
    public DishVO getByIdWithFlavor(Long id) {
        return dishMapper.getByIdWithFlavor(id);
    }

    /**
     * 修改菜品
     * @param dishDTO
     */
    @Override
    @Transactional
    public void updateWithFlavor(DishDTO dishDTO) {
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);
        Integer count = dishMapper.update(dish);
        if (count < 1) {
            // 起售的菜品不能修改
            throw new UpdateNotAllowedException(MessageConstant.DISH_ON_SALE);
        }

        Long dishId = dish.getId();
        dishFlavorMapper.deleteByDishId(dishId);

        List<DishFlavor> flavors = dishDTO.getFlavors();
        if (flavors != null && !flavors.isEmpty()) {
            flavors.forEach(flavor -> flavor.setDishId(dishId));
            dishFlavorMapper.saveBatch(flavors);
        }
        // 修改的商品是停售的，缓存已经被删除
    }

    /**
     * 起售停售菜品
     * @param status
     * @param id
     * @return categoryId
     */
    @Override
    public void startOrStop(Integer status, Long id) {
        Long categoryId = dishMapper.getCategoryId(id);;
        if (status.equals(StatusConstant.ENABLE)) {
            // 启售菜品时需要先检查其分类是否启用
            Integer categoryStatus = categoryMapper.getStatus(categoryId);
            if (categoryStatus.equals(StatusConstant.DISABLE)) {
                throw new EnableNotAllowedException(MessageConstant.DISH_ENABLE_FAILED);
            }
        }
        Dish dish = Dish.builder()
                .status(status)
                .id(id)
                .build();
        dishMapper.update(dish);
        // 删除该菜品分类的缓存
        String key = RedisConstant.DISH_CATEGORY_KEY + categoryId;
        stringRedisTemplate.delete(key);
    }

    /**
     * 菜品分类查询
     * @param categoryId
     * @return
     */
    @Override
    public List<Dish> listByType(Long categoryId) {
        return dishMapper.listByType(categoryId);
    }

    /**
     * 条件查询菜品和口味
     * @param categoryId
     * @return
     */
    public List<DishVO> listWithFlavor(Long categoryId) {
        // 查询Redis缓存
        String key = RedisConstant.DISH_CATEGORY_KEY + categoryId;
        String json = stringRedisTemplate.opsForValue().get(key);
        if ("".equals(json)) {
            // 命中空对象
            return null;
        }
        if (StringUtils.isNotBlank(json)) {
            // 命中缓存
            return JSON.parseArray(json, DishVO.class);
        }
        // 缓存不存在，查询数据库
        Dish dish = new Dish();
        dish.setCategoryId(categoryId);
        dish.setStatus(StatusConstant.ENABLE);// 查询起售中的菜品
        List<DishVO> list = dishMapper.listWithFlavor(dish);
        if (list.isEmpty()) {
            // 缓存穿透，缓存空对象
            stringRedisTemplate.opsForValue().set(key, "", RedisConstant.NULL_CACHE_TTL, TimeUnit.SECONDS);
            return null;
        }
        // 缓存该分类下所有菜品
        stringRedisTemplate.opsForValue().set(key, JSON.toJSONString(list));

        return list;
    }
}
