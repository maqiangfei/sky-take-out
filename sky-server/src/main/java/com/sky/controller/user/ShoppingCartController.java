package com.sky.controller.user;

import com.sky.context.BaseContext;
import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.ShoppingCart;
import com.sky.result.Result;
import com.sky.service.ShoppingCartService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.executor.statement.BaseStatementHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

/**
 * @author maqiangfei
 * @since 2024/10/8 下午8:59
 */
@RestController
@RequestMapping("/user/shoppingCart")
@Slf4j
@Api(tags = "C端购物车相关接口")
public class ShoppingCartController {

    @Autowired
    private ShoppingCartService shoppingCartService;

    /**
     * 添加购物车
     * @param shoppingCartDTO
     * @return
     */
    @PostMapping("/add")
    @ApiOperation("添加购物车")
    public Result add(@RequestBody ShoppingCartDTO shoppingCartDTO) {
        log.info("添加购物车：{}", shoppingCartDTO);
        shoppingCartService.add(shoppingCartDTO);
        return Result.success();
    }

    /**
     * 移除购物车
     * @param shoppingCartDTO
     * @return
     */
    @PostMapping("/sub")
    @ApiOperation("移除购物车")
    public Result sub(@RequestBody ShoppingCartDTO shoppingCartDTO) {
        log.info("移除购物车：{}", shoppingCartDTO);
        shoppingCartService.sub(shoppingCartDTO);
        return Result.success();
    }

    /**
     * 查询购物车
     * @return
     */
    @GetMapping("/list")
    @ApiOperation("查询购物车")
    public Result<List<ShoppingCart>> list() {
        log.info("查询购物车，用户id：{}", BaseContext.getCurrentId());
        List<ShoppingCart> list = shoppingCartService.list();
        list = list != null ? list : Collections.emptyList();
        return Result.success(list);
    }

    /**
     * 清除购物车
     * @return
     */
    @DeleteMapping("clean")
    @ApiOperation("清除购物车")
    public Result clear() {
        log.info("清除购物车，用户id：{}", BaseContext.getCurrentId());
        shoppingCartService.clear();
        return Result.success();
    }
}
