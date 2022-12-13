package com.macro.mall.common.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 校验工具类
 *
 */
public class ValidateUtil {


    /**
     * 中国手机号码
     */
    private static Pattern CHINESE_PHONE_PATTERN = Pattern.compile("((13|15|17|18)\\d{9})|(14[57]\\d{8})");


    /**
     * 是否是有效的中国手机号码
     *
     * @param phone 手机号
     * @return true:有效 false:无效
     */
    public static boolean isValidChinesePhone(String phone) {
        if (phone == null || phone.length() != 11) {
            return false;
        }

        Matcher matcher = CHINESE_PHONE_PATTERN.matcher(phone);
        return matcher.matches();
    }

    /**
     * 密码规则校验
     * 规则: 密码由6-16位长度的所有字符组成
     *
     * @param password 密码
     * @return true: 校验成功  false: 校验失败
     */
    public static boolean validatePassword(String password) {
        String regEx = "^.{6,16}$";
        Pattern pattern = Pattern.compile(regEx);
        Matcher matcher = pattern.matcher(password);
        return matcher.matches();
    }
}
