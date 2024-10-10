package com.sky.mapper;

import com.sky.entity.Orders;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author maqiangfei
 * @since 2024/10/9 下午9:04
 */
@Mapper
public interface OrderMapper {

    /**
     * 插入订单数据
     * @param orders
     */
    void insert(Orders orders);

    /**
     * 根据订单号和用户id查询订单id
     * @param outTradeNo
     * @param userId
     * @return
     */
    Long getIdByNumberAndUserId(String outTradeNo, Long userId);

    /**
     * 修改订单
     * @param orders
     */
    void update(Orders orders);
}
