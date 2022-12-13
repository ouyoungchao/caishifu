package com.macro.mall.portal.service;

/**
 * 短信服务
 */
public interface MessageService {

    /**
     * 发送短信
     * @param telephone
     * @return
     */
    public boolean sendMessage(String telephone);

}
