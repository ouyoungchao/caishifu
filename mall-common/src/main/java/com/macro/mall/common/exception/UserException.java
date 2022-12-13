package com.macro.mall.common.exception;

import com.macro.mall.common.api.ResultCode;

public class UserException extends CaiShiFuException {
    ResultCode resultCode;

    public UserException() {
    }

    public UserException(ResultCode resultCode) {
        this.resultCode = resultCode;
    }

    public ResultCode getResultCode() {
        return resultCode;
    }

    @Override
    public String getMessage() {
        return resultCode.getMessage();
    }
}
