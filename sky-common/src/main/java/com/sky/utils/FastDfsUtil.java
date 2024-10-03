package com.sky.utils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.csource.common.MyException;
import org.csource.common.NameValuePair;
import org.csource.fastdfs.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.IOException;


/**
 * @author maqiangfei
 * @since 2024/10/3 上午10:01
 */
@Slf4j
@RequiredArgsConstructor
public class FastDfsUtil {

    private final String propsFilePath;

    private String trackerHost;
    private TrackerServer trackerServer;

    private String httpPrefix = "http://";

    @PostConstruct
    public void init() {
        try {
            ClientGlobal.initByProperties(propsFilePath);
            this.trackerHost = ClientGlobal.getG_tracker_group().getTrackerServer().getInetSocketAddress().getHostName();
            trackerServer = new TrackerClient().getTrackerServer();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String upload(MultipartFile file) throws Exception {
        String originalFilename = file.getOriginalFilename();
        String extName = originalFilename.substring(originalFilename.lastIndexOf(".") + 1);

        StorageClient1 storageClient = new StorageClient1(trackerServer);

        NameValuePair[] nameValuePairs = new NameValuePair[1];
        nameValuePairs[0] = new NameValuePair("fileName", originalFilename);

        String path = null;
        try {
            path = storageClient.upload_file1(file.getBytes(), extName, nameValuePairs);
        } finally {
            storageClient.close();
        }
        return httpPrefix + trackerHost + "/" + path;
    }

    public String upload(byte[] bytes, String originalFilename) throws Exception {
        String extName = originalFilename.substring(originalFilename.lastIndexOf(".") + 1);

        StorageClient1 storageClient = new StorageClient1(trackerServer);

        NameValuePair[] nameValuePairs = new NameValuePair[1];
        nameValuePairs[0] = new NameValuePair("fileName", originalFilename);

        String path = null;
        try {
            path = storageClient.upload_file1(bytes, extName, nameValuePairs);
        } finally {
            storageClient.close();
        }
        return httpPrefix + trackerHost + "/" + path;
    }

    public NameValuePair[] getInfo(String fileId) throws Exception {
        StorageClient1 client1 = new StorageClient1(trackerServer);
        NameValuePair[] nameValuePairs = null;
        try {
            nameValuePairs = client1.get_metadata1(fileId);
        } finally {
            client1.close();
        }
        return nameValuePairs;
    }

    public void delete(String fileId) throws Exception {
        StorageClient1 client1 = new StorageClient1(trackerServer);
        try {
            client1.delete_file1(fileId);
        } finally {
            client1.close();
        }
    }
}
