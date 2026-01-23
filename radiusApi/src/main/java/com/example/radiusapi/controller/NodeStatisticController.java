package com.example.radiusapi.controller;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.radiusapi.entity.NodeStatistic;
import com.example.radiusapi.mapper.DbInformationMapper;
import com.example.radiusapi.mapper.MakeNodeStatisticMapper;
import com.example.radiusapi.mapper.NodeStatisticMapper;
import com.example.radiusapi.utils.Result;

import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
public class NodeStatisticController {

    @Autowired
    private NodeStatisticMapper mapper;

    @Autowired
    private MakeNodeStatisticMapper procMapper;

    @Operation(summary ="Get NodeStatistic ")
    @GetMapping("/NodeStatistic")

    public Result GetNodeStatistic(@RequestParam("node_id") String node_id) {
        log.info("GetNodeStatistic({})", node_id);

        QueryWrapper<NodeStatistic> wrapper = new QueryWrapper<NodeStatistic>();

        wrapper.eq("node_id", node_id);

        List<NodeStatistic> NodeStatisticList;
        Result ret = new Result();

        try {
            procMapper.make_node_statistic();
            NodeStatisticList = mapper.selectList(wrapper);

        } catch (DataAccessException e) {
            // 处理异常
            log.error(e.getMessage());
            ret.error();
            return ret;
        }

        ret.ok();
        ret.data("data", NodeStatisticList);
        return ret;
    }

}
