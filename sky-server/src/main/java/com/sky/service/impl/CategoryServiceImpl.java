package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.CategoryTypeConstant;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.CategoryDTO;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.entity.Category;
import com.sky.exception.DisableNotAllowedException;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.CategoryMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.CategoryService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author maqiangfei
 * @since 2024/10/2 下午3:43
 */
@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private SetmealMapper setmealMapper;

    @Override
    public void save(CategoryDTO categoryDTO) {
        Category category = new Category();
        BeanUtils.copyProperties(categoryDTO, category);

        category.setStatus(StatusConstant.DISABLE);
        // category.setCreateTime(LocalDateTime.now());
        // category.setUpdateTime(LocalDateTime.now());
        // category.setCreateUser(BaseContext.getCurrentId());
        // category.setUpdateUser(BaseContext.getCurrentId());

        categoryMapper.insert(category);
    }

    /**
     * 编辑分类信息
     * @param categoryDTO
     */
    @Override
    public void update(CategoryDTO categoryDTO) {
        Category category = new Category();
        BeanUtils.copyProperties(categoryDTO, category);

        // category.setUpdateTime(LocalDateTime.now());
        // category.setUpdateUser(BaseContext.getCurrentId());

        categoryMapper.update(category);
    }

    /**
     * 分页查询分类
     * @param categoryPageQueryDTO
     * @return
     */
    @Override
    public PageResult pageQuery(CategoryPageQueryDTO categoryPageQueryDTO) {
        PageHelper.startPage(categoryPageQueryDTO.getPage(), categoryPageQueryDTO.getPageSize());

        Page<Category> page = categoryMapper.pageQuery(categoryPageQueryDTO);

        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 启用禁用分类
     * @param status
     * @param id
     */
    @Override
    public void startOrStop(Integer status, Long id) {
        if (status.equals(StatusConstant.DISABLE)) {
            // 停用分类先检查其菜品或套餐是否有起售的
            Integer type = categoryMapper.getType(id);
            if (type.equals(CategoryTypeConstant.DISH_TYPE)) {
                // 菜品分类，去dish查
                Integer count = dishMapper.countByCategoryIdAndStatus(id, StatusConstant.ENABLE);
                if (count > 0) {
                    throw new DisableNotAllowedException(MessageConstant.DISH_CATEGORY_DISABLE_FAILED);
                }
            } else if (type.equals(CategoryTypeConstant.SETMEAL_TYPE)) {
                // 套餐分类，去setmeal查
                Integer count = setmealMapper.countByCategoryIdAndStatus(id, StatusConstant.ENABLE);
                if (count > 0) {
                    throw new DisableNotAllowedException(MessageConstant.SETMEAL_CATEGORY_DISABLE_FAILED);
                }
            }
        }
        Category category = Category.builder()
                .id(id)
                .status(status)
                // .updateTime(LocalDateTime.now())
                // .updateUser(BaseContext.getCurrentId())
                .build();
        categoryMapper.update(category);
    }

    /**
     * 根据id删除分类
     * @param id
     */
    @Override
    public void deleteById(Long id) {
        // 查询该分类是否关联了菜品
        Integer count = dishMapper.countByCategoryIdAndStatus(id, null);
        if (count > 0) {
            throw new DeletionNotAllowedException(MessageConstant.CATEGORY_BE_RELATED_BY_DISH);
        }
        // 查询该分类是否关联了套餐
        Integer count1 = setmealMapper.countByCategoryIdAndStatus(id, null);
        if (count1 > 0) {
            throw new DeletionNotAllowedException(MessageConstant.CATEGORY_BE_RELATED_BY_SETMEAL);
        }

        categoryMapper.deleteById(id);
    }

    /**
     * 根据类型查询分类
     * @param type
     * @return
     */
    @Override
    public List<Category> listByType(Integer type) {
        return categoryMapper.listByType(type);
    }
}
