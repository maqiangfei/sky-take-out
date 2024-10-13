package com.sky.test;

import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * 使用POI操作Excel文件
 * @since 2024/10/13 上午9:15
 * @author maqiangfei
 */
public class POITest {

    /**
     * 通过POI创建Excel文件并写入文件内容
     */
    public static void write() throws IOException {
        // 在内存中创建一个Excel文件
        XSSFWorkbook excel = new XSSFWorkbook();
        // 在Excel文件中创建一个sheet页
        XSSFSheet sheet = excel.createSheet("info");
        // 在Sheet中创建行对象, rownum编号从0开始
        XSSFRow row = sheet.createRow(1);
        // 创建单元格，并写入内容
        row.createCell(1).setCellValue("姓名");
        row.createCell(2).setCellValue("城市");
        // 创建一个新行
        row = sheet.createRow(2);
        row.createCell(1).setCellValue("张三");
        row.createCell(2).setCellValue("北京");

        row = sheet.createRow(3);
        row.createCell(1).setCellValue("李四");
        row.createCell(2).setCellValue("南京");

        FileOutputStream out = new FileOutputStream(new File("/Users/maffy/Documents/study/苍穹外卖/资料/day12/info.xlsx"));
        excel.write(out);
        // 关闭资源
        out.close();
        excel.close();
    }

    /**
     * 通过POI读取Excel文件的内容
     */
    public static void read() throws IOException {
        FileInputStream in = new FileInputStream(new File("/Users/maffy/Documents/study/苍穹外卖/资料/day12/info.xlsx"));
        XSSFWorkbook excel = new XSSFWorkbook(in);
        // 读取Excel文件的第一个Sheet页
        XSSFSheet sheet = excel.getSheetAt(0);
        // 获取Sheet页中最后一行的行号
        int lastRowNum = sheet.getLastRowNum(); // 获取的是索引

        for (int i = 1; i <= lastRowNum; i++) {
            XSSFRow row = sheet.getRow(i);
            short lastCellNum = row.getLastCellNum(); // 获取的是列数
            for (short j = 1; j < lastCellNum; j++) {
                String value = row.getCell(j).getStringCellValue();
                System.out.print(value + " ");
            }
            System.out.println();
        }
        //关闭资源
        excel.close();
        in.close();
    }

    public static void main(String[] args) throws IOException {
        // write();
        read();
    }
}
