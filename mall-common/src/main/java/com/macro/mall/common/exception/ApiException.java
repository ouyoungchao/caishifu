package com.macro.mall.common.exception;

import com.macro.mall.common.api.IErrorCode;

/**
 * 自定义API异常
 * Created by macro on 2020/2/27.
 */
public class ApiException extends RuntimeException {
    private IErrorCode errorCode;
    private String message;

    public ApiException(IErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    /**
     * 应用于错误弹窗提示，errorCode中的message作为title， message作为提示内容
     * @param errorCode
     * @param message
     */
    public ApiException(IErrorCode errorCode, String message) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.message = message;
    }

    public ApiException(String message) {
        super(message);
    }

    public ApiException(Throwable cause) {
        super(cause);
    }

    public ApiException(String message, Throwable cause) {
        super(message, cause);
    }

    public IErrorCode getErrorCode() {
        return errorCode;
    }

    /**
     * 用于放在响应的data中的message
     * @return
     */
    public String getDataMessage() { return this.message; }
}
