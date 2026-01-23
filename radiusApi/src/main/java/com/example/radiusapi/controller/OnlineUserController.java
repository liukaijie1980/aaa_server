package com.example.radiusapi.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import com.example.radiusapi.entity.OnlineUser;

import com.example.radiusapi.mapper.OnlineUserMapper;

import com.example.radiusapi.utils.Result;

import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@Slf4j
@RestController
public class OnlineUserController {

    @Autowired
    private OnlineUserMapper Mapper;
    @Operation(summary ="Get Online User List")
    @GetMapping("/OnlineUser")
    public Result GetOnlineUser(@RequestParam("name") String UserName,
                                @RequestParam("realm") String realm,
                                @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                @RequestParam(name = "pageSize", defaultValue = "3") Integer pageSize

    )
    {
        log.info("GetOnlineUser({},{})",UserName,realm);
   // public IPage GetOnlineUser(@PathVariable String UserName, @PathVariable String realm){
        QueryWrapper<OnlineUser> wrapper= new QueryWrapper<OnlineUser>();
        Page<OnlineUser> page=new Page<>(pageNo,pageSize);
        if (UserName.length()!=0)
        {
            wrapper.eq("username",UserName);
        }
            wrapper.eq("realm",realm);

        //online_user表有时会有离线用户的记录未及转移到offline_user表的情况，所以必须排除
            wrapper.isNull("acctstoptime");

        IPage<OnlineUser> iPage;
        Result ret=new Result();

        try {
            iPage=Mapper.selectPage(page,wrapper);
        } catch (DataAccessException e) {
            // 处理异常
            log.error(e.getMessage());
            ret.error();
            return ret;
        }
        ret.ok();
        ret.data("data",iPage);
        log.info("result={}",ret);
       return ret;
    }

    @Operation(summary ="Update  Online User List，this api can't use for modify user_name and realm")
    @PutMapping("/OnlineUser")
    public Result UpdateOnlineUser(@RequestBody OnlineUser user)
    {
        log.info("UpdateOnlineUser({})",user);
        Result ret=new Result();
        UpdateWrapper<OnlineUser> wrapper= new UpdateWrapper<>();
        //我们约定新的account 不能修改UserName和Realm，否则本函数需要额外传入这两个参数作为匹配条件。
        wrapper.eq("username",user.getUsername())
                .eq("acctsessionid",user.getAcctsessionid())
                .eq("realm",user.getRealm());

        int i;
        try {
            i=Mapper.update(user,wrapper);
        } catch (DataAccessException e) {
            // 处理异常
            log.error(e.getMessage());
            ret.error();
            return ret;
        }

        if (i>0) {
            ret.ok();
        }else {
            ret.error();
        }
        log.info("result={}",ret);
        return ret;
    }



}



