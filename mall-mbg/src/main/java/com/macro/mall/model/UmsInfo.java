package com.macro.mall.model;

import java.io.Serializable;

public class UmsInfo implements Serializable {
    private UmsLoginInfo umsLoginInfo;

    public UmsInfo(UmsLoginInfo loginInfo) {
        this.umsLoginInfo = loginInfo;
    }

    public UmsLoginInfo getUmsLoginInfo() {
        return umsLoginInfo;
    }

    public void setUmsLoginInfo(UmsLoginInfo umsLoginInfo) {
        this.umsLoginInfo = umsLoginInfo;
    }
}
