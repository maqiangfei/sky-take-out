package com.sky.service.impl;

import com.sky.bo.Sum4DateBO;
import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.service.ReportService;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author maqiangfei
 * @since 2024/10/12 下午12:22
 */
@Service
@Slf4j
public class ReportServiceImpl implements ReportService {

    @Autowired
    private OrderMapper orderMapper;

    /**
     * 统计指定时间区间内的营业额数据
     * @param begin
     * @param end
     * @return
     */
    @Override
    public TurnoverReportVO getTurnoverStatistics(LocalDate begin, LocalDate end) {
        List<LocalDate> dateList = new ArrayList<>();

        dateList.add(begin);

        LocalDate now = begin;
        while (now.isBefore(end)) {
            now = now.plusDays(1);
            dateList.add(now);
        }

        Map<String, Object> map = new HashMap<>();
        map.put("begin", begin);
        map.put("end", end);
        map.put("status", Orders.COMPLETED);

        List<Sum4DateBO> sum4DateBOS = orderMapper.sumByMap(map);
        int index = 0;
        List<Double> turnoverList = new ArrayList<>(dateList.size());
        for (LocalDate date : dateList) {
            if (index > sum4DateBOS.size() - 1) {
                turnoverList.add(0.0);
                continue;
            }
            turnoverList.add(date.equals(sum4DateBOS.get(index).getDate()) ? sum4DateBOS.get(index++).getSum() : 0.0);
        }

        return TurnoverReportVO.builder()
                .dateList(StringUtils.join(dateList, ","))
                .turnoverList(StringUtils.join(turnoverList, ","))
                .build();
    }

    @Override
    public UserReportVO getUserStatistics(LocalDate begin, LocalDate end) {
        // 统计每日新增用户数量
        List<Integer> newUserList = orderMapper.countUserByDateAndType(begin, end, 1);
        // 统计每日用户总数
        List<Integer> totalUserList = orderMapper.countUserByDateAndType(begin, end, 2);

        // 生成横坐标对应日期列表
        List<LocalDate> dateList = new ArrayList<>();
        dateList.add(begin);
        while (begin.isBefore(end)) {
            begin = begin.plusDays(1);
            dateList.add(begin);
        }

        return UserReportVO.builder()
                .dateList(StringUtils.join(dateList, ","))
                .newUserList(StringUtils.join(newUserList, ","))
                .totalUserList(StringUtils.join(totalUserList, ","))
                .build();
    }
}
