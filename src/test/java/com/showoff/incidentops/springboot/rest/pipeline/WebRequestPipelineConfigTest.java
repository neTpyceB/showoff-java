package com.showoff.incidentops.springboot.rest.pipeline;

import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class WebRequestPipelineConfigTest {
    @Test
    void config_registersApiRequestInterceptor() {
        ApiRequestInterceptor interceptor = new ApiRequestInterceptor();
        WebRequestPipelineConfig config = new WebRequestPipelineConfig(interceptor);
        InterceptorRegistry registry = new InterceptorRegistry();

        config.addInterceptors(registry);

        assertNotNull(config);
    }
}
