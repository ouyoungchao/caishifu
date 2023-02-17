package com.macro.mall.controller;

import com.macro.mall.common.api.CommonPage;
import com.macro.mall.service.UmsMemberService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @title MmsMemberController
 * @discription 会员管理控制器
 * @create by yhdjy 2023/2/16 20:09
 */

@RestController
@Api(tags = "UmsMemberController", description = "会员管理")
@RequestMapping("/member")
public class UmsMemberController {
    @Value("${jwt.tokenHeader}")
    private String tokenHeader;
    @Value("${jwt.tokenHead}")
    private String tokenHead;

    @Autowired
    private UmsMemberService memberService;

    @ApiOperation("会员列表")
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public CommonPage list(@RequestParam(value = "keyword", required = false) String keyword,
                           @RequestParam(value = "pageSize", defaultValue = "5") Integer pageSize,
                           @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum) {
        return memberService.list(keyword, pageSize, pageNum);
    }
}
