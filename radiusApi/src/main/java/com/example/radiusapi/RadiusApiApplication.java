package com.example.radiusapi;


import com.example.radiusapi.utils.GjpLogger;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
//import springfox.documentation.swagger2.annotations.EnableSwagger2;

import javax.annotation.PostConstruct;
import java.util.TimeZone;


@SpringBootApplication
@MapperScan("com.example.radiusapi.mapper")
@ComponentScan(basePackages = {"com.example"})
@Slf4j

public class RadiusApiApplication {
    @PostConstruct
    void started() {
        TimeZone.setDefault(TimeZone.getTimeZone("GMT+8"));
//TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
//TimeZone.setDefault(TimeZone.getTimeZone("Asia/Shanghai"));
    }

    public static void main(String[] args) {
        SpringApplication.run(RadiusApiApplication.class, args);

        GjpLogger.info("application is  started  .......");
    }
}