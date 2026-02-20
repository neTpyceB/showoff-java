package com.showoff.incidentops.springboot.rest.pipeline;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebRequestPipelineConfig implements WebMvcConfigurer {
    private final ApiRequestInterceptor apiRequestInterceptor;

    public WebRequestPipelineConfig(ApiRequestInterceptor apiRequestInterceptor) {
        this.apiRequestInterceptor = apiRequestInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(apiRequestInterceptor);
    }
}
