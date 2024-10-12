package com.sky.service.impl;

import com.google.common.collect.Lists;
import com.sky.bo.Sum4DateBO;
import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.service.ReportService;
import com.sky.vo.TurnoverReportVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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

        while (begin.isBefore(end)) {
            begin = begin.plusDays(1);
            dateList.add(begin);
        }

        LocalDateTime beginTime = LocalDateTime.of(dateList.get(0), LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(end, LocalTime.MAX);

        Map<String, Object> map = new HashMap<>();
        map.put("begin", beginTime);
        map.put("end", endTime);
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
}
