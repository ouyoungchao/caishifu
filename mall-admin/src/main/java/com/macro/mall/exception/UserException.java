package com.macro.mall.exception;

import com.macro.mall.common.api.ResultCode;
import com.macro.mall.exception.CaiShiFuException;

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
