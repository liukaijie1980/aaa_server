package com.example.radiusapi.config;
import com.example.radiusapi.controller.TokenInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ConcurrentTaskExecutor;
import org.springframework.web.servlet.config.annotation.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

@EnableWebMvc
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    //开启跨域

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // 设置允许跨域的路由
        registry.addMapping("/**")
                // 设置允许跨域请求的域名
                .allowedOriginPatterns("*")
                // 是否允许携带cookie参数
                .allowCredentials(true)
                // 设置允许的方法
                .allowedMethods("*")
                // 跨域允许时间
                .maxAge(3600);
    }


    private TokenInterceptor tokenInterceptor;
    //构造方法
    public WebMvcConfig(TokenInterceptor tokenInterceptor){
        this.tokenInterceptor = tokenInterceptor;
    }

    @Override
    public void configureAsyncSupport(AsyncSupportConfigurer configurer){
        configurer.setTaskExecutor(new ConcurrentTaskExecutor(Executors.newFixedThreadPool(3)));
        configurer.setDefaultTimeout(30000);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        List<String> excludePath = new ArrayList<>();
        //排除拦截，除了注册登录(此时还没token)，其他都拦截
        excludePath.add("/admin/login");     //注册
        excludePath.add("/admin/logout");  //注销，有时候token过期了，所以不校验注销

        //下面2行代码是为了使用swagger, 例如 http://ip:port/swagger-ui/index.html
        excludePath.add("/v3/api-docs/**");
        //注释下面才能使用api文档，但就失去登录功能。
        //excludePath.add("/**");


        excludePath.add("/img/**");  //静态资源
        excludePath.add("/song/**");  //静态资源

        registry.addInterceptor(tokenInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(excludePath);
        WebMvcConfigurer.super.addInterceptors(registry);
    }


    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("swagger-ui.html")
                .addResourceLocations("classpath:/META-INF/resources/");

        registry.addResourceHandler("/webjars/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/");
    }




}
