package com.macro.mall.service;

import com.macro.mall.common.api.CommonPage;
import com.macro.mall.model.UmsMember;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @title UmsMemberService
 * @description 会员管理
 * @create by yhdjy 2023/2/16 20:09
 */

@Component
public interface UmsMemberService {

    /**
     * 获取所有会员列表
     * @return
     */
    List<UmsMember> list();

    /**
     * 分页获取会员列表
     * @param keyword
     * @param pageSize
     * @param pageNum
     * @return
     */
    CommonPage<UmsMember> list(String keyword, Integer pageSize, Integer pageNum);

    /**
     * 锁定会员用户
     * @param ids
     * @return
     */
    int lock(List<Long> ids);
}
