package com.example.radiusapi.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

@Configuration
public class SpringDocConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("蓝杰网络宽带认证系统 api 文档")
                        .description("内部 api 文档")
                        .version("2.00"));
    }

    // 如果你需要 InternalResourceViewResolver，你仍然可以保留它
    @Bean
    public InternalResourceViewResolver defaultViewResolver() {
        return new InternalResourceViewResolver();
    }
}
