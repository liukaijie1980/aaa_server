package com.example.radiusapi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.radiusapi.entity.DbInformation;
import com.example.radiusapi.entity.NodeStatistic;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

@Repository
public interface MakeNodeStatisticMapper extends BaseMapper<NodeStatistic> {

    @Select("call make_node_statistic")
    void make_node_statistic();

}
