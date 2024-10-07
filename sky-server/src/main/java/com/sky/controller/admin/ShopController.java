package com.sky.controller.admin;

import com.sky.constant.RedisConstant;
import com.sky.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

/**
 * @author maqiangfei
 * @since 2024/10/6 下午2:22
 */
@RestController("adminShopController")
@RequestMapping("/admin/shop")
@Api(tags = "店铺相关接口")
@Slf4j
public class ShopController {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 修改店铺的营业状态
     * @param status
     * @return
     */
    @PutMapping("/{status}")
    @ApiOperation("修改店铺的营业状态")
    public Result setStatus(@PathVariable Integer status) {
        log.info("修改店铺的营业状态为: {}", status == 1 ? "营业中" : "打烊中");
        stringRedisTemplate.opsForValue().set(RedisConstant.SHOP_STATUS, status.toString());
        return Result.success();
    }

    /**
     * 查询店铺的营业状态
     * @return
     */
    @GetMapping("/status")
    @ApiOperation("查询店铺的营业状态")
    public Result getStatus() {
        String status = stringRedisTemplate.opsForValue().get(RedisConstant.SHOP_STATUS);
        int state = Integer.parseInt(status);
        log.info("查询店铺的营业状态为: {}", state == 1 ? "营业中" : "打烊中");
        return Result.success(state);
    }
}
