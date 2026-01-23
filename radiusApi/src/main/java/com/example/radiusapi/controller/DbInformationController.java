package com.example.radiusapi.controller;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.radiusapi.entity.DbInformation;
import com.example.radiusapi.mapper.DbInformationMapper;
import com.example.radiusapi.utils.Result;

import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;



@RestController

@Slf4j
public class DbInformationController {

    @Autowired
    private DbInformationMapper Mapper;

    @Operation(summary =" get Database Information ")
    @GetMapping("/DbInformation")
    public Result GetDbInformation(
            @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
            @RequestParam(name = "pageSize", defaultValue = "3") Integer pageSize
    )
    {
        log.info("GetDbInformation()");

        Page<DbInformation> page=new Page<>(pageNo,pageSize);

        IPage<DbInformation> iPage;
        Result ret=new Result();

        try {
            iPage= Mapper.selectPage(page);

        } catch (DataAccessException e) {
            // 处理异常
            log.error(e.getMessage());
            ret.error();
            return  ret;
        }

        ret.ok() ;
        ret.data("data",iPage);
        log.info("result={}",ret);
        return  ret;

    }


}
