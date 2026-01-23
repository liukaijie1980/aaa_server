package com.example.radiusapi.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.radiusapi.entity.Radpostauth;

import com.example.radiusapi.mapper.RadpostauthMapper;
import com.example.radiusapi.utils.Result;

import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;


@Slf4j
@RestController
public class RadpostauthController {

    @Autowired
    private RadpostauthMapper mapper;

    private static final DateTimeFormatter MYSQL_DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private String toMySqlDateTimeString(String isoDateTime) {
        if (isoDateTime != null && !isoDateTime.isEmpty()) {
            ZonedDateTime zdt = ZonedDateTime.parse(isoDateTime);
            return zdt.toLocalDateTime().format(MYSQL_DATETIME_FORMATTER);
        }
        return null;
    }

    @Operation(summary = "Get Radpostauth List")
    @GetMapping("/radpostauth")
    public Result getRadpostauth(
            @RequestParam("name") String userName,
            @RequestParam("realm") String realm,
            @RequestParam("reply") String reply,
            @RequestParam("callingstationid") String callingStationId,
            @RequestParam("nasidentifier") String nasIdentifier,
            @RequestParam("from") String dateFrom,
            @RequestParam("to") String dateTo,
            @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
            @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize
    ) {
        log.info("GetRadpostauth({}, {}, {}, {}, {}, {}, {})", userName, realm, reply, callingStationId, nasIdentifier, dateFrom, dateTo);

        QueryWrapper<Radpostauth> wrapper = new QueryWrapper<>();
        Page<Radpostauth> page = new Page<>(pageNo, pageSize);

        if (!userName.isEmpty()) {
            wrapper.eq("username", userName);
        }
        wrapper.eq("realm", realm);
        if (!reply.isEmpty()) {
            wrapper.eq("reply", reply);
        }
        if (!callingStationId.isEmpty()) {
            wrapper.eq("callingstationid", callingStationId);
        }
        if (!nasIdentifier.isEmpty()) {
            wrapper.eq("nasidentifier", nasIdentifier);
        }
        if (!dateFrom.isEmpty() && !dateTo.isEmpty()) {
            wrapper.ge("authdate", toMySqlDateTimeString(dateFrom));
            wrapper.le("authdate", toMySqlDateTimeString(dateTo));
        }

        IPage<Radpostauth> iPage;
        Result ret = new Result();

        try {
            iPage = mapper.selectPage(page, wrapper);
        } catch (DataAccessException e) {
            log.error(e.getMessage());
            ret.error();
            return ret;
        }
        ret.ok();
        ret.data("data", iPage);
        log.info("result={}", ret);
        return ret;
    }
}