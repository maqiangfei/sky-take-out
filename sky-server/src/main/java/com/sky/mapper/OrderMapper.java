package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.dto.GoodsSalesDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.entity.Orders;
import com.sky.vo.OrderVO;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

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

    /**
     * 条件分页查询
     * @param ordersPageQueryDTO
     * @return
     */
    Page<OrderVO> pageQuery(OrdersPageQueryDTO ordersPageQueryDTO);

    /**
     * 查询订单详细信息
     * @param id
     * @return
     */
    OrderVO getWithDetailById(Long id);

    /**
     * 各个状态的订单数量统计
     * @return
     */
    Integer countStatus(Integer status);

    /**
     * 根据id查询订单
     * @param id
     * @return
     */
    Orders getById(Long id);

    /**
     * 根据订单号查询订单
     * @param number
     * @return
     */
    Orders getByNumber(String number);

    /**
     * 根据订单状态和下单时间查询订单
     * @param status
     * @param orderTime
     * @return
     */
    List<Orders> getByStatusAndOrderTimeLT(Integer status, LocalDateTime orderTime);

    /**
     * 根据动态条件统计营业额数据
     * @param map
     * @return
     */
    List<Double> sumByMap(Map<String, Object> map);


    /**
     * 根据动态条件统计订单数量
     * @param map
     * @return
     */
    List<Integer> countByMap(Map<String, Object> map);

    /**
     * 统计指定时间区间的销量排名前10
     * @param begin
     * @param end
     * @return
     */
    List<GoodsSalesDTO> getSalesTop10(LocalDate begin, LocalDate end);
}
