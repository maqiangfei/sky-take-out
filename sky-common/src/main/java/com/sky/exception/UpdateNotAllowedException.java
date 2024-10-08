package com.sky.exception;

/**
 * @author maqiangfei
 * @since 2024/10/8 下午2:00
 */
public class UpdateNotAllowedException extends BaseException {

    public UpdateNotAllowedException() {}

    public UpdateNotAllowedException(String message) {
        super(message);
    }
}
