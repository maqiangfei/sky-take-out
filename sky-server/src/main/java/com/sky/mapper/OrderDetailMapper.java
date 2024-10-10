package com.sky.mapper;

import com.sky.entity.OrderDetail;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @author maqiangfei
 * @since 2024/10/9 下午9:05
 */
@Mapper
public interface OrderDetailMapper {

    /**
     * 批量插入订单明细
     * @param orderDetails
     */
    void insertBatch(List<OrderDetail> orderDetails);
}
