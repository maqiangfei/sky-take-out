package com.sky.service.impl;

import com.alibaba.fastjson.JSON;
import com.sky.constant.RedisConstant;
import com.sky.context.BaseContext;
import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.ShoppingCart;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * @author maqiangfei
 * @since 2024/10/8 下午9:04
 */
@Service
@Slf4j
public class ShoppingCartServiceImpl implements ShoppingCartService {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private SetmealMapper setmealMapper;

    @Autowired
    private DishMapper dishMapper;

    /**
     * 添加购物车
     * @param shoppingCartDTO
     */
    @Override
    public void add(ShoppingCartDTO shoppingCartDTO) {
        ShoppingCart shoppingCart = buildShoppingCart(shoppingCartDTO);
        add(shoppingCart);
    }

    /**
     * 添加购物车
     * @param shoppingCart
     */
    @Override
    public void add(ShoppingCart shoppingCart) {
        String key = RedisConstant.CART_USER_KEY + shoppingCart.getUserId();
        String json = stringRedisTemplate.opsForValue().get(key);
        List<ShoppingCart> shoppingCarts = JSON.parseArray(json, ShoppingCart.class);
        if (shoppingCarts != null) {
            for (ShoppingCart item : shoppingCarts) {
                if (item.getSetmealId() != null && item.getSetmealId().equals(shoppingCart.getSetmealId()) ||
                        item.getDishId() != null && item.getDishId().equals(shoppingCart.getDishId()) && item.getDishFlavor().equals(shoppingCart.getDishFlavor())) {
                    // 购物车中已经存在，数量加1
                    item.setNumber(item.getNumber() + 1);
                    stringRedisTemplate.opsForValue().set(key, JSON.toJSONString(shoppingCarts), RedisConstant.CART_TTL, TimeUnit.HOURS);
                    return;
                }
            }
        } else {
            shoppingCarts = new ArrayList<>();
        }
        shoppingCarts.add(shoppingCart);
        stringRedisTemplate.opsForValue().set(key, JSON.toJSONString(shoppingCarts), RedisConstant.CART_TTL, TimeUnit.HOURS);
    }

    /**
     * 移除购物车
     * @param shoppingCartDTO
     */
    @Override
    public void sub(ShoppingCartDTO shoppingCartDTO) {
        ShoppingCart shoppingCart = buildShoppingCart(shoppingCartDTO);
        String key = RedisConstant.CART_USER_KEY + BaseContext.getCurrentId();
        String json = stringRedisTemplate.opsForValue().get(key);
        List<ShoppingCart> shoppingCarts = JSON.parseArray(json, ShoppingCart.class);
        if (shoppingCarts == null) {
            return;
        }
        for (ShoppingCart item : shoppingCarts) {
            if (item.getSetmealId() != null && item.getSetmealId().equals(shoppingCart.getSetmealId()) ||
                    item.getDishId() != null && item.getDishId().equals(shoppingCart.getDishId()) && item.getDishFlavor().equals(shoppingCart.getDishFlavor())) {
                // 将其数量减1，如过为0，则删除
                item.setNumber(item.getNumber() - 1);
                if (item.getNumber() <= 0) {
                    shoppingCarts.remove(item);
                }
                stringRedisTemplate.opsForValue().set(key, JSON.toJSONString(shoppingCarts), RedisConstant.CART_TTL, TimeUnit.HOURS);
                return;
            }
        }
    }

    private ShoppingCart buildShoppingCart(ShoppingCartDTO shoppingCartDTO) {
        String dishFlavor = Optional.ofNullable(shoppingCartDTO.getDishFlavor()).orElse("");
        ShoppingCart shoppingCart;
        if (shoppingCartDTO.getSetmealId() != null) { // 添加套餐
            Setmeal setmeal = setmealMapper.getById(shoppingCartDTO.getSetmealId());
            shoppingCart = ShoppingCart.builder()
                    .setmealId(shoppingCartDTO.getSetmealId())
                    .name(setmeal.getName())
                    .image(setmeal.getImage())
                    .amount(setmeal.getPrice())
                    .build();
        } else {
            Dish dish = dishMapper.getById(shoppingCartDTO.getDishId());
            shoppingCart = ShoppingCart.builder()
                    .dishId(shoppingCartDTO.getDishId())
                    .dishFlavor(shoppingCartDTO.getDishFlavor())
                    .name(dish.getName())
                    .image(dish.getImage())
                    .dishFlavor(dishFlavor)
                    .amount(dish.getPrice())
                    .build();
        }
        shoppingCart.setUserId(BaseContext.getCurrentId());
        shoppingCart.setNumber(1);
        shoppingCart.setCreateTime(LocalDateTime.now());
        return shoppingCart;
    }

    /**
     * 查询购物车
     * @return
     */
    @Override
    public List<ShoppingCart> list() {
        String key = RedisConstant.CART_USER_KEY + BaseContext.getCurrentId();
        String json = stringRedisTemplate.opsForValue().get(key);
        List<ShoppingCart> shoppingCarts = JSON.parseArray(json, ShoppingCart.class);
        return shoppingCarts;
    }

    /**
     * 清除购物车
     */
    @Override
    public void clear() {
        String key = RedisConstant.CART_USER_KEY + BaseContext.getCurrentId();
        stringRedisTemplate.delete(key);
    }

}
