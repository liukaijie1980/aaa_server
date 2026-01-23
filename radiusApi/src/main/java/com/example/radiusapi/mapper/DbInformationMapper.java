package com.example.radiusapi.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.radiusapi.entity.DbInformation;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.sql.Wrapper;
import java.util.List;

@Repository
public interface DbInformationMapper extends BaseMapper<DbInformation> {
    @Select("SELECT file_name, concat(TOTAL_EXTENTS,'M') as 'File_size' FROM INFORMATION_SCHEMA.FILES order by TOTAL_EXTENTS DESC ")
    IPage<DbInformation> selectPage( IPage<DbInformation> page);
}

