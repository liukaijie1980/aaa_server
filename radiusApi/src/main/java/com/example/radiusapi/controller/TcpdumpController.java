package com.example.radiusapi.controller;
import com.example.radiusapi.utils.Result;
import com.example.radiusapi.utils.TcpdumpService;
import com.example.radiusapi.utils.TcpdumpWrapper;

import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;



@Slf4j
@RestController
public class TcpdumpController {
    @Autowired
    private TcpdumpService tcpdumpService;


    @Operation(summary ="run a system command and get result")
    @GetMapping("/RunCommand")
    //public Result GetAccountInfo(@PathVariable String UserName,@PathVariable String realm)
    public Result RunCommand(  )
    {
        log.info("RunCommand()");


        TcpdumpWrapper tcpdumpWrapper = new TcpdumpWrapper("c:\\windows\\System32\\cmd.exe  /c dir d:\\ ", 10);
        Result ret=new Result();
        try {
            String tcpdumpOutput = tcpdumpWrapper.capturePackets();
            System.out.println(tcpdumpOutput);
            ret.ok();
            ret.data("data",tcpdumpOutput);
            // 将tcpdumpOutput返回给前端
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            ret.error();
        }

        log.info("result={}",ret);
        return  ret;
    }


    @Operation(summary ="run tcpdump by ssh")
    @PostMapping("/tcpdump")
    public void runTcpdump(@RequestParam String user, @RequestParam String password, @RequestParam String host, @RequestParam int port) {
        log.info("tcpdump()");
        tcpdumpService.runTcpdump(user, password, host, port);

    }



}
