package com.slz.demo.common.exception;

import com.slz.demo.common.enumeration.ErrorCode;

/**
 * 业务异常，用于业务逻辑校验不通过时抛出
 */
public class BusinessException extends BaseException {

    public BusinessException(String message) {
        super(message);
    }

    public BusinessException(Integer code, String message) {
        super(code, message);
    }

    public BusinessException(ErrorCode errorCode) {
        super(errorCode);
    }
}
