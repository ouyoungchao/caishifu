package com.macro.mall.mapper;

import com.macro.mall.model.UmsLoginInfo;
import com.macro.mall.model.UmsAdminExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface UmsAdminMapper {
    long countByExample(UmsAdminExample example);

    int deleteByExample(UmsAdminExample example);

    int deleteByPrimaryKey(Long id);

    int insert(UmsLoginInfo record);

    int insertSelective(UmsLoginInfo record);

    List<UmsLoginInfo> selectByExample(UmsAdminExample example);

    UmsLoginInfo selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") UmsLoginInfo record, @Param("example") UmsAdminExample example);

    int updateByExample(@Param("record") UmsLoginInfo record, @Param("example") UmsAdminExample example);

    int updateByPrimaryKeySelective(UmsLoginInfo record);

    int updateByPrimaryKey(UmsLoginInfo record);
}