package com.sky.test;

import com.sky.utils.FastDfsUtil;
import org.csource.common.NameValuePair;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.*;

/**
 * @since 2024/10/3 下午1:27
 * @author maqiangfei
 */
@SpringBootTest
public class FastDfsUtilTest {

    @Autowired
    private FastDfsUtil fastDfsUtil;

    @Test
    void testGetInfo() throws Exception {
        NameValuePair[] info = fastDfsUtil.getInfo("group1/M00/00/00/rBBAgGb882yAAOZdAAAAcHCySKY95..txt");
        for (NameValuePair nameValuePair : info) {
            System.out.println(nameValuePair.getName() + "=" + nameValuePair.getValue());
        }
    }

    @Test
    void testDelete() throws Exception {
        fastDfsUtil.delete("group1/M00/00/00/rBBAgGb_ogWAdJS9AAASxCc7ac459.jpeg");
    }

}
