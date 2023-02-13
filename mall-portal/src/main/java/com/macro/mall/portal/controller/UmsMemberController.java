package com.macro.mall.portal.controller;

import com.macro.mall.common.api.CommonResult;
import com.macro.mall.common.api.ResultCode;
import com.macro.mall.common.api.ResultMessage;
import com.macro.mall.common.exception.CaiShiFuException;
import com.macro.mall.common.util.JsonUtil;
import com.macro.mall.common.util.ValidateUtil;
import com.macro.mall.model.UmsMember;
import com.macro.mall.model.UmsUser;
import com.macro.mall.portal.service.MessageService;
import com.macro.mall.portal.service.UmsMemberService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 会员登录注册管理Controller
 * Created by macro on 2018/8/3.
 */
@Controller
@Api(tags = "UmsMemberController")
@Tag(name = "UmsMemberController", description = "会员登录注册管理")
@RequestMapping("/caishifu")
public class UmsMemberController {
    private static final Logger LOGGER = LoggerFactory.getLogger(UmsMemberController.class);

    @Value("${jwt.tokenHeader}")
    private String tokenHeader;

    @Value("${jwt.tokenHead}")
    private String tokenHead;

    @Autowired
    private UmsMemberService memberService;

    @Autowired
    private MessageService messageService;

    @ApiOperation("会员注册")
    @RequestMapping(value = "/sso/register", method = RequestMethod.POST)
    @ResponseBody
    public CommonResult register(@RequestParam String nicName,
                                   @RequestParam String password,
                                   @RequestParam String telephone,
                                   @RequestParam String authCode,
                                   @RequestParam String isBuyer) {
        //参数是否合法
        if (!ValidateUtil.isValidChinesePhone(telephone) || (password.trim().isEmpty() || authCode.trim().isEmpty())) {
            return CommonResult.failed(ResultMessage.ERROR_PARAM);
        }
        try {
            memberService.register(nicName, password, telephone, authCode, isBuyer);
            return CommonResult.success(ResultMessage.REGISTER_SUCCESS);
        } catch (CaiShiFuException e) {
            LOGGER.error("Register user failed ", e);
            return CommonResult.failed(ResultMessage.REGISTER_FAILED);
        }
    }

    @ApiOperation("会员登录")
    @RequestMapping(value = "/sso/login", method = RequestMethod.POST)
    @ResponseBody
    public CommonResult login(@RequestParam String telephone,
                                @RequestParam String password,
                                @RequestParam String isAuthCode) {
        if (!ValidateUtil.isValidChinesePhone(telephone)) {
            return CommonResult.failed(ResultMessage.ERROR_PARAM);
        }
        String token = null;
        try {
            token = memberService.login(telephone, password, Boolean.getBoolean(isAuthCode));

        } catch (CaiShiFuException e) {
            LOGGER.warn("Login failed ",e);
            return CommonResult.failed(e.getMessage());
        }
        Map<String, Object> tokenMap = new HashMap<>();
        tokenMap.put("token", token);
        tokenMap.put("tokenHead", tokenHead);
        return CommonResult.success(tokenMap, ResultCode.SUCCESS);
    }

    @ApiOperation("获取会员信息")
    @RequestMapping(value = "/userInfo", method = RequestMethod.GET)
    @ResponseBody
    public CommonResult getUserInfo(HttpServletRequest request) {
        String token = request.getHeader(tokenHeader).substring(tokenHead.length());
        UmsUser userDetails = null;
        try {
            userDetails = memberService.loadUserByToken(token);
            return CommonResult.success(userDetails, ResultCode.SUCCESS);
        } catch (CaiShiFuException e) {
            LOGGER.warn("getUserInfo failed ",e);
            return CommonResult.failed(e.getMessage());
        }
    }

    @ApiOperation("获取验证码")
    @RequestMapping(value = "/getAuthCode", method = RequestMethod.GET)
    @ResponseBody
    public CommonResult getAuthCode(@RequestParam String telephone) {
        if (!ValidateUtil.isValidChinesePhone(telephone)) {
            return CommonResult.failed(ResultMessage.ERROR_PHONE);
        }
        try {
            if (messageService.sendMessage(telephone)) {
                return CommonResult.success(null,"GET_VERIFICATION_SUCCESS");
            }
            return CommonResult.failed(ResultMessage.GET_VERIFICATION_FAILED);
        } catch (CaiShiFuException e) {
            LOGGER.warn("getAuthCode failed ",e);
            return CommonResult.failed(e.getMessage());
        }
    }

    @ApiOperation("会员修改密码")
    @RequestMapping(value = "/updatePassword", method = RequestMethod.POST)
    @ResponseBody
    public CommonResult updatePassword(@RequestParam String telephone,
                                       @RequestParam String password,
                                       @RequestParam String authCode) {
        memberService.updatePassword(telephone, password, authCode);
        return CommonResult.success(null, "密码修改成功");
    }


    @ApiOperation(value = "刷新token")
    @RequestMapping(value = "/refreshToken", method = RequestMethod.GET)
    @ResponseBody
    public CommonResult refreshToken(HttpServletRequest request) {
        String token = request.getHeader(tokenHeader).substring(tokenHead.length());
        String refreshToken = memberService.refreshToken(token);
        if (refreshToken == null) {
            return CommonResult.failed("token已经过期！");
        }
        Map<String, String> tokenMap = new HashMap<>();
        tokenMap.put("token", refreshToken);
        tokenMap.put("tokenHead", tokenHead);
        return CommonResult.success(tokenMap);
    }

    @ApiOperation("更新用户信息")
    @RequestMapping(value = "/updateMember", method = RequestMethod.POST)
    @ResponseBody
    public CommonResult updateMember(HttpServletRequest request) {
        String token = request.getHeader(tokenHeader).substring(tokenHead.length());
        try {
            List<MultipartFile> files = ((MultipartHttpServletRequest) request).getFiles("file");
            //获取用户信息
            UmsUser user = JsonUtil.readValue(request.getParameter("user"), UmsUser.class);
            if (user == null && files.isEmpty()) {
                //同时为null则为参数错误
                LOGGER.warn("用户修改信息不完整");
                return CommonResult.failed(ResultMessage.ERROR_PARAM);
            }
            UmsMember umsMember = new UmsMember();
            if(user != null) {
                memberService.copyUserToMember(user, umsMember);
                umsMember = memberService.updateMember(umsMember, null, token);
            } else {
                //上传头像
                umsMember = memberService.updateMember(umsMember, files.get(0), token);
                user = new UmsUser();
            }
            memberService.copyMemberToUser(umsMember,user);
            return CommonResult.success(user,ResultCode.SUCCESS);
        } catch (CaiShiFuException e) {
            LOGGER.error("Update member UserException ", e);
            return CommonResult.failed(e.getMessage());
        }
    }


}
