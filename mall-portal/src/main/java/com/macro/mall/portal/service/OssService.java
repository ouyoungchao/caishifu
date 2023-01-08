package com.macro.mall.portal.service;

import java.io.InputStream;

public interface OssService<T> {

    /**
     * 上传文件
     * @param bucketName
     * @param fileName
     * @param inputStream
     * @return String 返回url
     */
    public String uploadFile(String bucketName, String fileName, InputStream inputStream);

    public T downloadFile(String bucketName, String fileName);
}
