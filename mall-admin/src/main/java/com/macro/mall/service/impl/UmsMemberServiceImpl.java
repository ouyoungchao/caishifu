package com.macro.mall.service.impl;

import cn.hutool.core.util.StrUtil;
import com.github.pagehelper.PageHelper;
import com.macro.mall.common.api.CommonPage;
import com.macro.mall.mapper.UmsMemberMapper;
import com.macro.mall.model.UmsMember;
import com.macro.mall.model.UmsMemberExample;
import com.macro.mall.service.UmsMemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @title UmsMemberServiceImpl
 * @discription 会员管理service
 * @create by yhdjy 2023/2/16 20:09
 **/

@Service
public class UmsMemberServiceImpl implements UmsMemberService {

    @Autowired
    private UmsMemberMapper memberMapper;

    @Override
    public List<UmsMember> list() {
        return memberMapper.selectByExample(new UmsMemberExample());
    }

    @Override
    public CommonPage<UmsMember> list(String keyword, Integer pageSize, Integer pageNum) {
        PageHelper.startPage(pageNum, pageSize);
        UmsMemberExample memberExample = new UmsMemberExample();
        if (StrUtil.isNotEmpty(keyword)) {
            memberExample.createCriteria().andPhoneLike(keyword);
        }
        return CommonPage.restPage(memberMapper.selectByExample(memberExample));
    }

    @Override
    public int lock(List<Long> ids) {
        return 0;
    }
}
