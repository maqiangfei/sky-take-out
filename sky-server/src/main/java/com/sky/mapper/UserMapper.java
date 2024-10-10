package com.sky.mapper;

import com.sky.entity.User;
import org.apache.ibatis.annotations.Mapper;

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
}
