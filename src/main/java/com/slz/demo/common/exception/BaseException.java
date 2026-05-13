package com.slz.demo.common.exception;

import com.slz.demo.common.enumeration.ErrorCode;
import lombok.Getter;

/**
 * 异常基类，所有自定义异常均继承此类
 * 通过 {@code code} 字段将异常映射为统一响应状态码
 */
@Getter
public class BaseException extends RuntimeException {

    private Integer code;

    public BaseException(String message) {
        super(message);
    }

    public BaseException(Integer code, String message) {
        super(message);
        this.code = code;
    }

    public BaseException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
    }
}
