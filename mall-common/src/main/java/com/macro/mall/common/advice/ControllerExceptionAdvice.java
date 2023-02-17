package com.macro.mall.common.advice;

import com.macro.mall.common.api.CommonResult;
import com.macro.mall.common.api.ResultCode;
import com.macro.mall.common.exception.ApiException;
import org.springframework.validation.BindException;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 统一异常响应包装
 * BindException为标签参数验证异常
 * ApiException为接口业务异常
 * @title ControllerExceptionAdvice
 * @description
 * @create by yhdjy 2023/2/17 15:39
 **/

@RestControllerAdvice
public class ControllerExceptionAdvice {

    /**
     * 参数校验失败统一响应
     * @param e
     * @return
     */
    @ExceptionHandler({BindException.class})
    public CommonResult MethodArgumentNotValidExceptionHandler(BindException e) {
        // 从异常对象中拿到ObjectError对象
        ObjectError objectError = e.getBindingResult().getAllErrors().get(0);
        return CommonResult.failed(ResultCode.VALIDATE_FAILED, objectError.getDefaultMessage());
    }

    /**
     * API业务异常统一响应
     * @param e
     * @return
     */
    @ExceptionHandler({ApiException.class})
    public CommonResult ApiExceptionHandler(ApiException e) {
        return CommonResult.failed(e.getErrorCode(), e.getMessage(), e.getDataMessage());
    }
}
