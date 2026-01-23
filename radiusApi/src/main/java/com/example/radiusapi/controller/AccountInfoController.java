package com.example.radiusapi.controller;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.radiusapi.entity.AccountInfo;

import com.example.radiusapi.mapper.AccountInfoMapper;

import com.example.radiusapi.utils.Result;

import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@Slf4j
@RestController
public class AccountInfoController {
    @Autowired
    private AccountInfoMapper AccountMapper;

    //@ApiOperation("Get Account infomation ")
    @Operation(summary = "Get Account infomation", description = "Provides a greeting message")
    @GetMapping("/AccountInfo")
    //public Result GetAccountInfo(@PathVariable String UserName,@PathVariable String realm)
    public Result GetAccountInfo(  @RequestParam("name") String UserName, @RequestParam("realm") String realm ,
                                  @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                   @RequestParam(name = "pageSize", defaultValue = "3") Integer pageSize
    )
    {
        log.info("GetAccountInfo({},{})",UserName,realm);

        Page<AccountInfo> page=new Page<>(pageNo,pageSize);
        QueryWrapper<AccountInfo> wrapper= new QueryWrapper<AccountInfo>();
       if (UserName.length()!=0)  {
           wrapper.eq("user_name",UserName);
       }
        wrapper.eq("realm",realm);
        IPage<AccountInfo> iPage;

        Result ret=new Result();
        try {
            iPage= AccountMapper.selectPage(page,wrapper);
        } catch (DataAccessException e) {
            // 处理异常
            log.error(e.getMessage());
            ret.error();
            return ret;
        }
        ret.ok();
        ret.data("data",iPage);
        log.info("result={}",ret);
        return  ret;
    }

    @Operation(summary = "add an Account ")
    @PostMapping("/AccountInfo")
    public Result AddAccount( @RequestBody AccountInfo account )
    {
        log.info("AddAccount({})",account);

        Result ret=new Result();
        int i=0;
        try {
            i=AccountMapper.insert(account);
        } catch (DataAccessException e) {
            // 处理异常
            log.error(e.getMessage());
            ret.error();
            return ret;
        }
        if (i>0)    {
            ret.ok();

        }else    {
            ret.error();
        }
        log.info("result={}",ret);
        return  ret;
    }

    @Operation(summary = "Update Account infomation ，this api can't use for modify user_name and realm")
    @PutMapping("/AccountInfo")
    public Result UpdateAccount(@RequestBody AccountInfo account)
    {
        log.info("UpdateAccount({})",account);
        Result ret=new Result();
        UpdateWrapper<AccountInfo> wrapper= new UpdateWrapper<>();
        //我们约定新的account 不能修改UserName和Realm，否则本函数需要额外传入这两个参数作为匹配条件。
        wrapper.eq("user_name",account.getUserName())
                .eq("realm",account.getRealm());

        int i;
        try {
            i=AccountMapper.update(account,wrapper);
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


    @Operation(summary = "delete an Account")
    @DeleteMapping("/AccountInfo")
    public Result DeleteAccount(@RequestParam("UserName") String UserName, @RequestParam("realm") String realm )
    {
       log.info("DeleteAccount({},{})",UserName,realm);

        Result ret=new Result();
        UpdateWrapper<AccountInfo> wrapper= new UpdateWrapper<>();
         wrapper.eq("user_name",UserName)
                .eq("realm",realm);
        int i;
        try {
            i=AccountMapper.delete(wrapper);
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
