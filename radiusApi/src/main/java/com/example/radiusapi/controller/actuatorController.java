package com.example.radiusapi.controller;


import com.example.radiusapi.utils.Result;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.info.GitProperties;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
@Slf4j
@RestController
public class actuatorController {

    @Autowired
    private GitProperties git;

    @Operation(summary = "Get getActuator infomation")
    @GetMapping("/actuator-info")
    public Result getActuatorInfo() {

        log.info("git.getCommitId({})",git.getCommitId());
        Result ret=new Result();
        ret.ok();
        ret.data("data",git);
        return ret;


    }
}