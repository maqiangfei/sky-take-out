package com.sky.config;

import com.sky.utils.FastDfsUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author maqiangfei
 * @since 2024/10/3 下午12:45
 */
@Configuration
@Slf4j
public class FastDfsConfig {

    @Bean
    public FastDfsUtil fastDfsUtil(
            @Value("${sky.fastdfs.props-file-path}") String propsFilePath) {
        log.info("创建fastdfs文件上传工具类对象");
        return new FastDfsUtil(propsFilePath);
    }
}
