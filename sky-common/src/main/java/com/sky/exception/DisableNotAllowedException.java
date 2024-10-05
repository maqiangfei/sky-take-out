package com.sky.exception;

/**
 * @author maqiangfei
 * @since 2024/10/4 下午2:23
 */
public class DisableNotAllowedException extends BaseException {

    public DisableNotAllowedException() {}

    public DisableNotAllowedException(String message) {
        super(message);
    }
}
