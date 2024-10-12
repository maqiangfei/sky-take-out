package com.sky.mapper;

import com.sky.entity.User;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDate;
import java.util.List;

/**
 * @author maqiangfei
 * @since 2024/10/7 下午1:44
 */
@Mapper
public interface UserMapper {

    /**
     * 根据openid查询用户
     * @param openId
     * @return
     */
    User getByOpenId(String openId);

    /**
     * 插入用户
     * @param user
     */
    void insert(User user);

    /**
     * 根据id查询用户
     * @param id
     * @return
     */
    User getById(Long id);

    /**
     * 根据日期统计用户
     * @param begin
     * @param end
     * @param userType
     * @return
     */
    List<Integer> countUserByTypeAndDate(LocalDate begin, LocalDate end, Integer userType);
}
