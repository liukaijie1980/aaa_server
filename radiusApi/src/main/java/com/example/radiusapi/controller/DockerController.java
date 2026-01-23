package com.example.radiusapi.controller;
import com.example.radiusapi.utils.DockerService;
import com.example.radiusapi.utils.Result;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@Slf4j
@RestController

@RequestMapping("/docker")
public class DockerController {
    @Autowired
    private DockerService dockerService;

    @PostMapping("/restart")
    @Operation(summary ="restart radius docker service in docker swarm")
    public   Result restartDockerService() {

        Result ret=new Result();
        try {
            String  URL="http://192.168.245.128:2375/services/myradius_radius";
            //dockerService.restartDockerService(URL);
            dockerService.restartDockerService();
            log.info("Service restarted successfully.");
        } catch (Exception e) {
            log.error("Failed to restart service: " + e.getMessage());
            ret.error();
            ret.data("data", e.getMessage());
            return ret;
        }
        ret.ok();
        log.info("result={}",ret);
        return ret;
    }
}

