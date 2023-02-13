package com.macro.mall.common.exception;

public class CaiShiFuException extends Throwable{

    public String message;

    public CaiShiFuException() {
    }

    public CaiShiFuException(String message) {
        super(message);
        this.message = message;
    }

    public CaiShiFuException(Throwable cause) {
        super(cause);
    }
}
