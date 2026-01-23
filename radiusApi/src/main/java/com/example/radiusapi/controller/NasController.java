package com.example.radiusapi.controller;


import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.example.radiusapi.entity.Nas;

import com.example.radiusapi.mapper.NasMapper;

import com.example.radiusapi.utils.DockerService;
import com.example.radiusapi.utils.Result;

import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@Slf4j
@RestController
public class NasController {
    @Autowired
    private NasMapper Mapper;
    @Autowired
    private DockerService dockerService;

    @Operation(summary = "get list of  nas ")
    @GetMapping("/nas")
    public Result GetNas(   )
    {
        log.info("GetNas()");


        List<Nas> NasList;
        Result ret=new Result();

        try {
            NasList= Mapper.selectList(null);

        } catch (DataAccessException e) {
            // 处理异常
            log.error(e.getMessage());
            ret.error();
            return  ret;
        }

        ret.ok() ;
        ret.data("data",NasList);
        log.info("result={}",ret);
        return  ret;

    }


    @Operation(summary = "add a Nas by pass a Nas Object")
    @PostMapping("/nas")
    public Result AddNas( @RequestBody Nas nas )
    {
        log.info("AddNas({})",nas);

        int i=0;
        try {
            i=Mapper.insert(nas);
        } catch (DataAccessException e) {
            // 处理异常
            log.error(e.getMessage());
        }
        Result ret=new Result();
        if (i>0)
        {
            ret.ok();
        }else
        {
            ret.error();
        }
        log.info("result={}",ret);
        return ret;
    }

    @Operation(summary ="Update Nas information")
    @PutMapping("/nas")
    public Result UpdateNas(@RequestBody Nas nas)
    {
        log.info("UpdateNas({})",nas);
        UpdateWrapper<Nas> wrapper= new UpdateWrapper<>();
        wrapper.eq("id",nas.getId());

        int i=0;
        try {
            i=Mapper.update(nas,wrapper);
        } catch (DataAccessException e) {
            // 处理异常
            log.error(e.getMessage());
        }

        Result ret=new Result();
        if (i>0) {
            ret.ok();
        }else            {
            ret.error();
        }
        log.info("result={}",ret);

        return ret;
    }


    @Operation(summary ="delete Realm by input id ")
    @DeleteMapping("/nas")
    public Result DeleteNas( @RequestParam("id") int id)
    {
        log.info("DeleteNas({})",id);

        UpdateWrapper<Nas> wrapper= new UpdateWrapper<>();
        wrapper.eq("id",id);

        int i=0;
        try {
            i=Mapper.delete(wrapper);
        } catch (DataAccessException e) {
            // 处理异常
            log.error(e.getMessage());
        }
        Result ret=new Result();
        if (i>0) {
            ret.ok();
        }else            {
            ret.error();
        }
        log.info("result={}",ret);


        return ret;
    }


}
