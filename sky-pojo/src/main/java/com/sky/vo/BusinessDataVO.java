package com.sky.vo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 数据概览
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BusinessDataVO implements Serializable {

    private Double turnover = 0d;//营业额

    private Integer validOrderCount = 0;//有效订单数

    private Double orderCompletionRate = 0d;//订单完成率

    private Double unitPrice = 0d;//平均客单价

    private Integer newUsers = 0;//新增用户数

    @JsonIgnore
    private Integer totalOrderCount = 0;//订单总数

    public BusinessDataVO sum(BusinessDataVO businessDataVO) {
        return BusinessDataVO.builder()
                .turnover(this.turnover + businessDataVO.getTurnover())
                .totalOrderCount(this.totalOrderCount + businessDataVO.getTotalOrderCount())
                .validOrderCount(this.validOrderCount + businessDataVO.getValidOrderCount())
                .newUsers(this.newUsers + businessDataVO.getNewUsers())
                .build();
    }
}
