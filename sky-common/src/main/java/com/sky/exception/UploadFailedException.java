package com.sky.exception;

/**
 * @author maqiangfei
 * @since 2024/10/8 下午4:46
 */
public class UploadFailedException extends BaseException {

    public UploadFailedException() {}

    public UploadFailedException(String message) {
        super(message);
    }
}
