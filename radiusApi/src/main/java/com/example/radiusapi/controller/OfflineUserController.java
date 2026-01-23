package com.example.radiusapi.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.radiusapi.entity.OfflineUser;
import com.example.radiusapi.mapper.OfflineUserMapper;
import com.example.radiusapi.utils.Result;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@RestController
public class OfflineUserController {

    @Autowired
    private OfflineUserMapper mapper;

    private static final DateTimeFormatter MYSQL_DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private String toMySqlDateTimeString(String isoDateTime) {
        if (isoDateTime != null && !isoDateTime.isEmpty()) {
            ZonedDateTime zdt = ZonedDateTime.parse(isoDateTime);
            return zdt.toLocalDateTime().format(MYSQL_DATETIME_FORMATTER);
        }
        return null;
    }

    @Operation(summary = "Get Offline User List")
    @GetMapping("/OfflineUser")
    public Result getOfflineUser(
            @RequestParam("name") String userName,
            @RequestParam("realm") String realm,
            @RequestParam("framedipaddress") String framedIpAddress,
            @RequestParam("callingstationid") String callingStationId,
            @RequestParam("from") String dateFrom,
            @RequestParam("to") String dateTo,
            @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
            @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize
    ) {
        log.info("GetOfflineUser({}, {}, {}, {}, {}, {})", userName, realm, framedIpAddress, callingStationId, dateFrom, dateTo);

        QueryWrapper<OfflineUser> wrapper = new QueryWrapper<>();
        Page<OfflineUser> page = new Page<>(pageNo, pageSize);

        if (!userName.isEmpty()) {
            wrapper.eq("username", userName);
        }
        wrapper.eq("realm", realm);
        if (!framedIpAddress.isEmpty()) {
            wrapper.eq("framedipaddress", framedIpAddress);
        }
        if (!callingStationId.isEmpty()) {
            wrapper.eq("callingstationid", callingStationId);
        }
        if (!dateFrom.isEmpty() && !dateTo.isEmpty()) {
            String mysqlDateFrom = toMySqlDateTimeString(dateFrom);
            String mysqlDateTo = toMySqlDateTimeString(dateTo);
            /*
           我们的目的是选出在指定时间段（from，to），在线的用户
                            from ------ to         ||        from ------ to             ||         from ------ to
                                 start------stop         start-----stop                      start------------------stop
           &&(  (DateFrom<= acctstarttime <=DateTo )||(DateFrom<= acctstoptime <=DateTo)||(acctstarttime<=DateFrom && acctstoptime >=DateTo) )
          */
            wrapper.and(w -> w
                    .ge("acctstarttime", mysqlDateFrom).le("acctstarttime", mysqlDateTo)
                    .or()
                    .ge("acctstoptime", mysqlDateFrom).le("acctstoptime", mysqlDateTo)
                    .or()
                    .le("acctstarttime", mysqlDateFrom).ge("acctstoptime", mysqlDateTo));
        }

        IPage<OfflineUser> iPage;
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
