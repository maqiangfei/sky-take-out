package com.sky.controller.user;

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
@RestController("userShopController")
@RequestMapping("/user/shop")
@Api(tags = "店铺相关接口")
@Slf4j
public class ShopController {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 查询店铺的营业状态
     * @return
     */
    @GetMapping("/status")
    @ApiOperation("查询店铺的营业状态")
    public Result getStatus() {
        String status = stringRedisTemplate.opsForValue().get(RedisConstant.SHOP_STATUS_KEY);
        int state = Integer.parseInt(status);
        log.info("查询店铺的营业状态为: {}", state == 1 ? "营业中" : "打烊中");
        return Result.success(state);
    }
}
