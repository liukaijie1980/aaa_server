package com.example.radiusapi.controller;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.example.radiusapi.entity.Realm;
import com.example.radiusapi.mapper.RealmMapper;
import com.example.radiusapi.utils.Result;

import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@Slf4j
@RestController
public class RealmController {
    @Autowired
    private RealmMapper Mapper;


    @Operation(summary ="input node_id(sidebarTree) to get list of  Realm ")
    @GetMapping("/realm")
    public Result GetRealms(     @RequestParam("node_id") String PID   )
    {
        log.info("GetRealms({})",PID);

        QueryWrapper<Realm> wrapper = new QueryWrapper<Realm>();

        wrapper.eq("node_id", PID);

       List<Realm> realmList;
        Result ret=new Result();

        try {
            realmList= Mapper.selectList(wrapper);

        } catch (DataAccessException e) {
            // 处理异常
            log.error(e.getMessage());
            ret.error();
            return  ret;
        }

        ret.ok() ;
        ret.data("data",realmList);
        log.info("result={}",ret);
        return  ret;

    }


    @Operation(summary ="add a Realm by pass a Realm Object")
    @PostMapping("/realm")
    public Result AddRealm( @RequestBody Realm realm )
    {
        log.info("AddRealm({})",realm);

        int i=0;
        try {
            i=Mapper.insert(realm);
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

    @Operation(summary ="Update Realm information")
    @PutMapping("/realm")
    public Result UpdateRealm(@RequestBody Realm realm)
    {
        log.info("UpdateRealm({})",realm);
        UpdateWrapper<Realm> wrapper= new UpdateWrapper<>();
        wrapper.eq("realm",realm.getRealm());

        int i=0;
        try {
            i=Mapper.update(realm,wrapper);
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


    @Operation(summary ="delete Realm by input the realm ")
    @DeleteMapping("/realm")
    public Result DeleteRealm( @RequestParam("realm") String realm)
    {
        log.info("DeleteRealm({})",realm);

        UpdateWrapper<Realm> wrapper= new UpdateWrapper<>();
        wrapper.eq("realm",realm);

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
