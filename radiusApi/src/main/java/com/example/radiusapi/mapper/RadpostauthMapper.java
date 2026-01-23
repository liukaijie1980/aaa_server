package com.example.radiusapi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.radiusapi.entity.Radpostauth;
import org.apache.ibatis.annotations.Mapper;



@Mapper
public interface RadpostauthMapper extends BaseMapper<Radpostauth> {
    // @Select("Select * from UserInfo")
    // public List<UserInfo> find();
}