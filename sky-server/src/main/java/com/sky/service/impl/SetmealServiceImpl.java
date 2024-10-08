package com.sky.service.impl;

import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.LogConstant;
import com.sky.constant.MessageConstant;
import com.sky.constant.RedisConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.exception.EnableNotAllowedException;
import com.sky.exception.UpdateNotAllowedException;
import com.sky.mapper.CategoryMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.SetmealService;
import com.sky.utils.FastDfsUtil;
import com.sky.vo.DishItemVO;
import com.sky.vo.SetmealVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author maqiangfei
 * @since 2024/10/4 下午1:15
 */
@Service
@Slf4j
public class SetmealServiceImpl implements SetmealService {

    @Autowired
    private SetmealMapper setmealMapper;

    @Autowired
    private SetmealDishMapper setmealDishMapper;

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private FastDfsUtil fastDfsUtil;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 修改套餐和其与菜品的关联
     * @param setmealDTO
     */
    @Override
    @Transactional
    public void updateWithSetmealDish(SetmealDTO setmealDTO) {
        // 更新套餐信息
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO, setmeal);
        Integer count = setmealMapper.update(setmeal);
        if (count < 1) {
            // 起售的商品无法修改
            throw new UpdateNotAllowedException(MessageConstant.SETMEAL_ON_SALE);
        }

        Long setmealId = setmeal.getId();
        // 删除套餐与菜品的关联
        setmealDishMapper.deleteBySetmealIds(Arrays.asList(setmealId));

        // 插入套餐与菜品新的关联
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        if (setmealDishes != null && !setmealDishes.isEmpty()) {
            setmealDishes.forEach(setmealDish -> setmealDish.setSetmealId(setmealId));
            setmealDishMapper.saveBatch(setmealDishes);
        }
        // 更新套餐时为禁售状态，缓存已经被删除
    }

    /**
     * 根据id查询套餐
     * @param id
     * @return
     */
    @Override
    public SetmealVO getByIdWithSetmealDish(Long id) {
        return setmealMapper.getByIdWithSetmealDish(id);
    }

    /**
     * 启售停售套餐
     * @param status
     * @param id
     * @return categoryId
     */
    @Override
    public void startOrStop(Integer status, Long id) {
        Long categoryId = setmealMapper.getCategoryId(id);
        if (status.equals(StatusConstant.ENABLE)) {
            // 启用套餐先检查分类是否开启
            Integer categoryStatus = categoryMapper.getStatus(categoryId);
            if (categoryStatus != null && categoryStatus.equals(StatusConstant.DISABLE)) {
                throw new EnableNotAllowedException(MessageConstant.SETMEAL_ENABLE_FAILED);
            }
        }
        Setmeal setmeal = Setmeal.builder()
                .status(status)
                .id(id)
                .build();
        setmealMapper.update(setmeal);

        // 删除该分类下的套餐缓存
        String key = RedisConstant.SETMEAL_CATEGORY_KEY + categoryId;
        stringRedisTemplate.delete(key);
        // 删除该套餐的菜品缓存
        key = RedisConstant.SETMEAL_DISHITEM_KEY + id;
        stringRedisTemplate.delete(key);
    }

    /**
     * 根据id删除套餐及和菜品的关系
     * @param ids
     */
    @Override
    @Transactional
    public void deleteBatchWithSetmealDish(List<Long> ids) {
        // 查询是否有套餐起售
        Integer count = setmealMapper.countEnabledByIds(ids);
        if (count > 0) {
            throw new DeletionNotAllowedException(MessageConstant.SETMEAL_ON_SALE);
        }
        // 清理套餐的图片
        List<String> images = setmealMapper.getImageByIds(ids);
        images.forEach(image -> {
            String fileId = image.substring(image.indexOf("/", 8) + 1);
            if (fastDfsUtil.delete(fileId) > 0)
                log.error(LogConstant.CLEAR_SETMEAL_PIC_ERROR + ",{}", fileId);
        });
        // 删除套餐
        setmealMapper.deleteByIds(ids);
        // 删除和菜品的关系
        setmealDishMapper.deleteBySetmealIds(ids);
        // 删除的所有商品都应为禁售状态，缓存已经被删除
    }

    /**
     * 条件查询
     * @param categoryId
     * @return
     */
    @Override
    public List<Setmeal> list(Long categoryId) {
        // 从Redis中查询缓存
        String key = RedisConstant.SETMEAL_CATEGORY_KEY + categoryId;
        String json = stringRedisTemplate.opsForValue().get(key);
        if ("".equals(json)) {
            // 命中空对象
            return null;
        }
        if (StringUtils.isNotBlank(json)) {
            // 命中缓存
            return JSON.parseArray(json, Setmeal.class);
        }
        Setmeal setmeal = Setmeal.builder()
                .categoryId(categoryId)
                .status(StatusConstant.ENABLE)
                .build();
        List<Setmeal> list = setmealMapper.list(setmeal);
        if (list.isEmpty()) {
            // 缓存穿透，缓存空对象
            stringRedisTemplate.opsForValue().set(key, "", RedisConstant.NULL_CACHE_TTL, TimeUnit.SECONDS);
            return null;
        }
        // 缓存该分类的套餐商品
        stringRedisTemplate.opsForValue().set(key, JSON.toJSONString(list));
        return list;
    }

    /**
     * 根据id查询菜品选项
     * @param id
     * @return
     */
    @Override
    public List<DishItemVO> getDishItemById(Long id) {
        // 从Redis中查询套餐的商品缓存
        String key = RedisConstant.SETMEAL_DISHITEM_KEY + id;
        String json = stringRedisTemplate.opsForValue().get(key);
        if ("".equals(json)) {
            return null;
        }
        if (StringUtils.isNotBlank(json)) {
            // 命中缓存
            return JSON.parseArray(json, DishItemVO.class);
        }
        List<DishItemVO> list = setmealMapper.getDishItemBySetmealId(id);
        if (list.isEmpty()) {
            stringRedisTemplate.opsForValue().set(key, "", RedisConstant.NULL_CACHE_TTL, TimeUnit.SECONDS);
        }
        // 缓存该套餐的商品
        stringRedisTemplate.opsForValue().set(key, JSON.toJSONString(list));

        return list;
    }

    /**
     * 新增套餐
     * @param setmealDTO
     */
    @Override
    @Transactional
    public void saveWithSetmealDish(SetmealDTO setmealDTO) {
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO, setmeal);
        setmealMapper.save(setmeal);

        Long setmealId = setmeal.getId();

        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        if (setmealDishes != null && !setmealDishes.isEmpty()) {
            setmealDishes.forEach(setmealDish -> setmealDish.setSetmealId(setmealId));
            setmealDishMapper.saveBatch(setmealDishes);
        }
        // 新增套餐默认没有起售，所有不需要删除缓存
    }

    /**
     * 分页查询套餐
     * @param setmealPageQueryDTO
     * @return
     */
    @Override
    public PageResult pageQuery(SetmealPageQueryDTO setmealPageQueryDTO) {
        PageHelper.startPage(setmealPageQueryDTO.getPage(), setmealPageQueryDTO.getPageSize());

        Page<SetmealVO> page = setmealMapper.pageQuery(setmealPageQueryDTO);
        return new PageResult(page.getTotal(), page.getResult());
    }

}
