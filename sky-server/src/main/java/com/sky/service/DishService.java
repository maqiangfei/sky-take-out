package com.sky.service;

import com.sky.dto.DishDTO;

/**
 * @author maqiangfei
 * @since 2024/10/3 上午9:24
 */
public interface DishService {

    /**
     * 新增菜品和对应口味
     * @param dishDTO
     */
    void saveWithFlavor(DishDTO dishDTO);
}
