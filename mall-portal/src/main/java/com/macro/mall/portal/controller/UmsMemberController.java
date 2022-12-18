package com.macro.mall.portal.controller;

import com.macro.mall.common.api.CommonResult;
import com.macro.mall.common.api.ResultCode;
import com.macro.mall.common.exception.CaiShiFuException;
import com.macro.mall.common.exception.UserException;
import com.macro.mall.common.util.ValidateUtil;
import com.macro.mall.model.UmsMember;
import com.macro.mall.portal.service.MessageService;
import com.macro.mall.portal.service.UmsMemberService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.security.Principal;
import java.util.HashMap;
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
    public ResponseEntity register(@RequestParam String nicName,
                                   @RequestParam String password,
                                   @RequestParam String telephone,
                                   @RequestParam String authCode,
                                   @RequestParam String isBuyer) {
        //参数是否合法
        if(!ValidateUtil.isValidChinesePhone(telephone) || (password.trim().isEmpty() || authCode.trim().isEmpty())){
            return new ResponseEntity(CommonResult.failed(ResultCode.PARAM_ERROR), HttpStatus.INTERNAL_SERVER_ERROR);
        }
        try {
            memberService.register(nicName, password, telephone, authCode, isBuyer);
            return new ResponseEntity(CommonResult.success(ResultCode.REGISTER_USER_SUCCESS), HttpStatus.OK);
        } catch (UserException e) {
            LOGGER.error("Register user failed ", e);
            return new ResponseEntity(CommonResult.failed(e.getResultCode(), e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @ApiOperation("会员登录")
    @RequestMapping(value = "/sso/login", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity login(@RequestParam String telephone,
                              @RequestParam String password,
                              @RequestParam String isAuthCode) {
        if(!ValidateUtil.isValidChinesePhone(telephone)){
            return new ResponseEntity(CommonResult.failed(ResultCode.PARAM_ERROR), HttpStatus.INTERNAL_SERVER_ERROR);
        }
        String token = null;
        try {
            token = memberService.login(telephone, password, Boolean.getBoolean(isAuthCode));

        } catch (UserException e) {
            return new ResponseEntity(CommonResult.failed(e.getResultCode()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
        Map<String, Object> tokenMap = new HashMap<>();
        tokenMap.put("token", token);
        tokenMap.put("tokenHead", tokenHead);
        return new ResponseEntity(CommonResult.success(tokenMap, ResultCode.LOGIN_SUCCESS), HttpStatus.OK);
    }

    @ApiOperation("获取会员信息")
    @RequestMapping(value = "/info", method = RequestMethod.GET)
    @ResponseBody
    public CommonResult info(Principal principal) {
        if (principal == null) {
            return CommonResult.unauthorized(null);
        }
        UmsMember member = memberService.getCurrentMember();
        return CommonResult.success(member);
    }

    @ApiOperation("获取验证码")
    @RequestMapping(value = "/getAuthCode", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity getAuthCode(@RequestParam String telephone) {
        if(!ValidateUtil.isValidChinesePhone(telephone)){
            return new ResponseEntity(CommonResult.failed(ResultCode.PARAM_ERROR), HttpStatus.INTERNAL_SERVER_ERROR);
        }
        try{
            if(messageService.sendMessage(telephone)) {
                return new ResponseEntity(CommonResult.success(null, ResultCode.VERIFICATION_GET_SUCCESS), HttpStatus.OK);
            }
            return new ResponseEntity(CommonResult.failed( ResultCode.VERIFICATION_GET_FAILED), HttpStatus.OK);
        } catch (CaiShiFuException e){
            return new ResponseEntity(CommonResult.failed( ResultCode.VERIFICATION_GET_FAILED), HttpStatus.OK);
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
        String token = request.getHeader(tokenHeader);
        String refreshToken = memberService.refreshToken(token);
        if (refreshToken == null) {
            return CommonResult.failed("token已经过期！");
        }
        Map<String, String> tokenMap = new HashMap<>();
        tokenMap.put("token", refreshToken);
        tokenMap.put("tokenHead", tokenHead);
        return CommonResult.success(tokenMap);
    }
}
