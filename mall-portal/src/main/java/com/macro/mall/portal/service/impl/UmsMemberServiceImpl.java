package com.macro.mall.portal.service.impl;

import cn.hutool.core.util.StrUtil;
import com.macro.mall.common.api.ResultCode;
import com.macro.mall.common.exception.Asserts;
import com.macro.mall.common.exception.CaiShiFuException;
import com.macro.mall.common.exception.UserException;
import com.macro.mall.mapper.UmsMemberLevelMapper;
import com.macro.mall.mapper.UmsMemberMapper;
import com.macro.mall.model.*;
import com.macro.mall.portal.domain.MemberDetails;
import com.macro.mall.portal.service.UmsMemberCacheService;
import com.macro.mall.portal.service.UmsMemberService;
import com.macro.mall.portal.service.OssService;
import com.macro.mall.security.util.JwtTokenUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Date;
import java.util.List;

/**
 * 会员管理Service实现类
 * Created by macro on 2018/8/3.
 */
@Service
public class UmsMemberServiceImpl implements UmsMemberService {
    private static final Logger LOGGER = LoggerFactory.getLogger(UmsMemberServiceImpl.class);

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private UmsMemberMapper memberMapper;

    @Autowired
    private UmsMemberLevelMapper memberLevelMapper;

    @Autowired
    private UmsMemberCacheService memberCacheService;

    @Autowired
    private OssService ossService;

    @Value("${redis.key.authCode}")
    private String REDIS_KEY_PREFIX_AUTH_CODE;

    @Value("${redis.expire.authCode}")
    private Long AUTH_CODE_EXPIRE_SECONDS;

    @Value("${oss.bucket.useravatar}")
    private String OSS_BUCKET_USERAVATAR;

    @Override
    public UmsMember getByTelephone(String telephone) {
        UmsMember member = memberCacheService.getMemberByTelephone(telephone);
        if (member != null) return member;
        UmsMemberExample example = new UmsMemberExample();
        example.createCriteria().andPhoneEqualTo(telephone);
        List<UmsMember> memberList = memberMapper.selectByExample(example);
        if (!CollectionUtils.isEmpty(memberList)) {
            member = memberList.get(0);
            member.setUsername(telephone);
            memberCacheService.setMember(member);
            return member;
        }
        return null;
    }

    @Override
    public UmsMember getById(Long id) {
        return memberMapper.selectByPrimaryKey(id);
    }

    @Override
    public void register(String nicName, String password, String telephone, String authCode, String isBuyer) throws UserException {
        //验证验证码
        if (!verifyAuthCode(authCode, telephone)) {
            LOGGER.warn("%s %s %s", telephone, ResultCode.VERIFICATION_CODE_INVALID.getMessage(), authCode);
            throw new UserException(ResultCode.VERIFICATION_CODE_INVALID);
        }
        //查询是否已有该用户
        UmsMemberExample example = new UmsMemberExample();
        example.createCriteria().andPhoneEqualTo(telephone);
        List<UmsMember> umsMembers = memberMapper.selectByExample(example);
        if (!CollectionUtils.isEmpty(umsMembers)) {
            LOGGER.warn("User " + telephone + " has exist");
            throw new UserException(ResultCode.REGISTER_FAILED_USER_EXIST);
        }
        //没有该用户进行添加操作
        UmsMember umsMember = new UmsMember();
        umsMember.setNickname(nicName);
        umsMember.setPhone(telephone);
        umsMember.setPassword(passwordEncoder.encode(password));
        umsMember.setIsbuyer(isBuyer);
        umsMember.setCreateTime(new Date());
        umsMember.setStatus(1);
        //获取默认会员等级并设置
        UmsMemberLevelExample levelExample = new UmsMemberLevelExample();
        levelExample.createCriteria().andDefaultStatusEqualTo(1);
        List<UmsMemberLevel> memberLevelList = memberLevelMapper.selectByExample(levelExample);
        if (!CollectionUtils.isEmpty(memberLevelList)) {
            umsMember.setMemberLevelId(memberLevelList.get(0).getId());
        }
        memberMapper.insert(umsMember);
        umsMember.setPassword(null);
    }

    @Override
    public void updatePassword(String telephone, String password, String authCode) {
        UmsMemberExample example = new UmsMemberExample();
        example.createCriteria().andPhoneEqualTo(telephone);
        List<UmsMember> memberList = memberMapper.selectByExample(example);
        if (CollectionUtils.isEmpty(memberList)) {
            Asserts.fail("该账号不存在");
        }
        //验证验证码
        if (!verifyAuthCode(authCode, telephone)) {
            Asserts.fail("验证码错误");
        }
        UmsMember umsMember = memberList.get(0);
        umsMember.setPassword(passwordEncoder.encode(password));
        memberMapper.updateByPrimaryKeySelective(umsMember);
        memberCacheService.delMember(umsMember.getId());
    }

    @Override
    public UmsMember updateMember(UmsMember member, MultipartFile file, String token) throws UserException {
        String telephone = jwtTokenUtil.getUserNameFromToken(token);
        if(member == null){
            member = new UmsMember();
        }
        if (file != null) {
            if (!(file.getOriginalFilename().endsWith(".jpg") || file.getOriginalFilename().endsWith(".png"))) {
                LOGGER.warn("Upload file is error {}", file);
                throw new UserException(ResultCode.USERINFO_UPDATE_AVATAR_SUFFIX_ERROR);
            }
            String suffix = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf("."), file.getOriginalFilename().length());
            String fileName = telephone + (new Date().getTime()) + suffix;
            LOGGER.info("upload file " + file.getOriginalFilename());
            try {
                String url = ossService.uploadFile(OSS_BUCKET_USERAVATAR, fileName, file.getInputStream());
                member.setIcon(url);
            } catch (IOException e) {
                throw new UserException(ResultCode.USERINFO_UPDATE_AVATAR_CONTENT_ERROR);
            }
        }
        UmsMemberExample example = new UmsMemberExample();
        example.createCriteria().andPhoneEqualTo(telephone);
        memberMapper.updateByExampleSelective(member,example);
        return getByTelephone(telephone);
    }

    @Override
    public UmsMember getCurrentMember() {
        SecurityContext ctx = SecurityContextHolder.getContext();
        Authentication auth = ctx.getAuthentication();
        MemberDetails memberDetails = (MemberDetails) auth.getPrincipal();
        return memberDetails.getUmsMember();
    }

    @Override
    public void updateIntegration(Long id, Integer integration) {
        UmsMember record = new UmsMember();
        record.setId(id);
        record.setIntegration(integration);
        memberMapper.updateByPrimaryKeySelective(record);
        memberCacheService.delMember(id);
    }

    @Override
    public UserDetails loadUserByTelephone(String username) {
        UmsMember member = getByTelephone(username);
        if (member != null) {
            return new MemberDetails(member);
        }
        throw new UsernameNotFoundException("用户名或密码错误");
    }

    @Override
    public UmsUser loadUserByToken(String token) throws UserException {
        String telephone = jwtTokenUtil.getUserNameFromToken(token);
        try {
            MemberDetails member = (MemberDetails) loadUserByTelephone(telephone);
            UmsUser user = new UmsUser();
            copyMemberToUser(member.getUmsMember(), user);
            return user;
        } catch (AuthenticationException e) {
            throw new UserException(ResultCode.USERINFO_GET_FAILED);
        }
    }


    @Override
    public String login(String telephone, String password, boolean isAuthCode) throws UserException {
        //验证验证码
        if (isAuthCode) {
            if (!verifyAuthCode(password, telephone)) {
                LOGGER.warn("%s %s %s", telephone, ResultCode.VERIFICATION_CODE_INVALID.getMessage(), password);
                throw new UserException(ResultCode.VERIFICATION_CODE_INVALID);
            }
        }
        String token = null;
        //密码需要客户端加密后传递
        try {
            UserDetails userDetails = this.loadUserByTelephone(telephone);
            if (!isAuthCode && !passwordEncoder.matches(password, userDetails.getPassword())) {
                throw new UserException(ResultCode.LOGIN_USERNAME_OR_PASSWOR_ERROR);
            }
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authentication);
            token = jwtTokenUtil.generateToken(userDetails);
        } catch (AuthenticationException e) {
            LOGGER.warn("登录异常:{}", e.getMessage());
            throw new UserException(ResultCode.LOGIN_ERROR);
        }
        return token;
    }

    @Override
    public String refreshToken(String token) {
        return jwtTokenUtil.refreshHeadToken(token);
    }

    //对输入的验证码进行校验
    private boolean verifyAuthCode(String authCode, String telephone) {
        if (StrUtil.isEmpty(authCode)) {
            return false;
        }
        String realAuthCode = memberCacheService.getAuthCode(telephone);
        return authCode.equals(realAuthCode);
    }

    private void copyMemberToUser(UmsMember member, UmsUser user) {
        BeanUtils.copyProperties(member, user);
        user.setUserId(member.getId().toString());
        user.setUserAvatar(member.getIcon());
        user.setUserNickName(member.getNickname());
        user.setIsBuyer(member.getIsbuyer());
        user.setUserPhone(member.getPhone());
        user.setStatus(member.getStatus());
    }

}
