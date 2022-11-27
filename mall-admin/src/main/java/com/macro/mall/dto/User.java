package com.macro.mall.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;

/**
 * 用户注册参数
 * Created by macro on 2018/4/26.
 */
@Getter
@Setter
@ToString
public class User {

    @ApiModelProperty(value = "用户id")
    private String id;

    @NotEmpty
    @ApiModelProperty(value = "密码", required = true)
    private String password;
    
    @ApiModelProperty(value = "用户头像")
    private String icon;

    @NotEmpty
    @ApiModelProperty(value = "电话")
    private String telephone;

    @NotEmpty
    @ApiModelProperty(value = "用户昵称")
    private String nickName;

    @ApiModelProperty(value = "备注")
    private String note;

    @ApiModelProperty(value = "买家")
    private int isBuyer;
}
