package com.macro.mall.portal.service;

import com.macro.mall.common.exception.CaiShiFuException;

/**
 * 短信服务
 */
public interface MessageService {

    /**
     * 发送短信
     * @param telephone
     * @return
     */
    public boolean sendMessage(String telephone) throws CaiShiFuException;

}
