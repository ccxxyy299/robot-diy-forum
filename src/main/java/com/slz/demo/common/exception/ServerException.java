package com.slz.demo.common.exception;

import com.slz.demo.common.enumeration.ErrorCode;

/**
 * 服务器异常，用于系统运行错误时抛出
 */
public class ServerException extends BaseException {

    public ServerException(String message) {
        super(message);
    }

    public ServerException(Integer code, String message) {
        super(code, message);
    }
    public ServerException(ErrorCode errorCode) {
        super(errorCode);
    }
}
