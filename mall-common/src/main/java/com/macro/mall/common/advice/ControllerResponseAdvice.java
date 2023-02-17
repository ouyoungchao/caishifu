package com.macro.mall.common.advice;

import com.macro.mall.common.api.CommonResult;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * 统一响应包装，Controller直接返回数据或者CommonPage分页数据
 * 没想出怎么判断是否分页数据，所以这里没有对分页数据进行包装
 * @title ControllerResponseAdvice
 * @description
 * @create by yhdjy 2023/2/17 16:26
 **/

@RestControllerAdvice
public class ControllerResponseAdvice implements ResponseBodyAdvice<Object> {
    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        // response是ResultVo类型，或者注释了NotControllerResponseAdvice都不进行包装
        return !returnType.getParameterType().isAssignableFrom(CommonResult.class);
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType, Class<? extends HttpMessageConverter<?>> selectedConverterType, ServerHttpRequest request, ServerHttpResponse response) {
        // 否则直接包装成ResultVo返回
        return CommonResult.success(body);
    }
}

