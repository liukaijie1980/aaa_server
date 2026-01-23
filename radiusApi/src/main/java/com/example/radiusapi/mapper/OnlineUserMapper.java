package com.example.radiusapi.mapper;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.radiusapi.entity.OnlineUser;
import com.example.radiusapi.entity.AccountInfo;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface OnlineUserMapper extends BaseMapper<OnlineUser> {


}
