package com.sky.service;

import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.ShoppingCart;

import java.util.List;

/**
 * @author maqiangfei
 * @since 2024/10/8 下午9:03
 */
public interface ShoppingCartService {

    /**
     * 添加购物车
     * @param shoppingCartDTO
     */
    void add(ShoppingCartDTO shoppingCartDTO);

    /**
     * 添加购物车
     * @param shoppingCart
     */
    void add(ShoppingCart shoppingCart);

    /**
     * 移除购物车
     * @param shoppingCartDTO
     */
    void sub(ShoppingCartDTO shoppingCartDTO);

    /**
     * 查询购物车
     * @return
     */
    List<ShoppingCart> list();

    /**
     * 清除购物车
     */
    void clear();

}
