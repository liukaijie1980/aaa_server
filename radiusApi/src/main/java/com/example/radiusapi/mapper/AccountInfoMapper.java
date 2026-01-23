package com.example.radiusapi.mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.radiusapi.entity.AccountInfo;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AccountInfoMapper extends BaseMapper<AccountInfo> {
   // @Select("Select * from UserInfo")
   // public List<UserInfo> find();
}
