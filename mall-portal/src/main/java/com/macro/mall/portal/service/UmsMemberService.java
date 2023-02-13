package com.macro.mall.portal.service;

import com.macro.mall.common.exception.CaiShiFuException;
import com.macro.mall.model.UmsMember;
import com.macro.mall.model.UmsUser;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

/**
 * 会员管理Service
 * Created by macro on 2018/8/3.
 */
public interface UmsMemberService {
    /**
     * 根据用户名获取会员
     */
    UmsMember getByTelephone(String telephone);

    /**
     * 根据会员编号获取会员
     */
    UmsMember getById(Long id);

    /**
     * 用户注册
     */
    @Transactional
    void register(String nicName, String password, String telephone, String authCode, String isBuyer) throws CaiShiFuException;


    /**
     * 修改密码
     */
    @Transactional
    void updatePassword(String telephone, String password, String authCode);

    @Transactional
    UmsMember updateMember(UmsMember umsMember, MultipartFile file, String token) throws CaiShiFuException;

    /**
     * 获取当前登录会员
     */
    UmsMember getCurrentMember();

    /**
     * 根据会员id修改会员积分
     */
    void updateIntegration(Long id,Integer integration);


    /**
     * 获取用户信息
     */
    UserDetails loadUserByTelephone(String telephone);

    /**
     * 获取用户信息
     */
    UmsUser loadUserByToken(String token) throws CaiShiFuException;


    /**
     * 登录后获取token
     */
    String login(String telephone, String password, boolean isAuthCode) throws CaiShiFuException;

    /**
     * 刷新token
     */
    String refreshToken(String token);

    public void copyMemberToUser(UmsMember member, UmsUser user);

    public void copyUserToMember(UmsUser user, UmsMember member);
}
