package com.sky.utils;

import com.sky.constant.MessageConstant;
import com.sky.exception.UploadFailedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    private final String httpPrefix;

    private String trackerHost;
    private TrackerServer trackerServer;


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

    public String upload(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        String extName = originalFilename == null ? "" :
                originalFilename.substring(originalFilename.lastIndexOf(".") + 1);

        StorageClient1 storageClient = new StorageClient1(trackerServer);

        NameValuePair[] nameValuePairs = new NameValuePair[1];
        nameValuePairs[0] = new NameValuePair("fileName", originalFilename);

        String path;
        try {
            path = storageClient.upload_file1(file.getBytes(), extName, nameValuePairs);
        } catch (Exception e) {
            throw new UploadFailedException(MessageConstant.UPLOAD_FAILED);
        } finally {
            try {
                storageClient.close();
            } catch (IOException e) {
                log.error("图片删除失败：{}", e.getMessage());
            }
        }
        return httpPrefix + "://" + trackerHost + "/" + path;
    }

    public NameValuePair[] getInfo(String fileId) {
        StorageClient1 client1 = new StorageClient1(trackerServer);
        NameValuePair[] nameValuePairs;
        try {
            nameValuePairs = client1.get_metadata1(fileId);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            try {
                client1.close();
            } catch (IOException e) {
                log.error(e.getMessage());
            }
        }
        return nameValuePairs;
    }

    public int delete(String fileId) {
        StorageClient1 client1 = new StorageClient1(trackerServer);
        try {
            return client1.delete_file1(fileId);
        } catch (Exception e) {
            return 1;
        } finally {
            try {
                client1.close();
            } catch (IOException e) {
                log.error(e.getMessage());
            }
        }
    }
}
