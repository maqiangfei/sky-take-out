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
    void testUpload() throws Exception {
        FileInputStream fileInputStream = new FileInputStream("/Users/maffy/Documents/temp/temp.txt");
        byte[] bytes = new byte[fileInputStream.available()];
        fileInputStream.read(bytes);
        String path = fastDfsUtil.upload(bytes, "temp.txt");
        System.out.println(path);
    }

    @Test
    void testGetInfo() throws Exception {
        NameValuePair[] info = fastDfsUtil.getInfo("group1/M00/00/00/rBBAgGb882yAAOZdAAAAcHCySKY95..txt");
        for (NameValuePair nameValuePair : info) {
            System.out.println(nameValuePair.getName() + "=" + nameValuePair.getValue());
        }
    }

    @Test
    void testDelete() throws Exception {
        fastDfsUtil.delete("group1/M00/00/00/rBBAgGb882yAAOZdAAAAcHCySKY95..txt");
    }

}
