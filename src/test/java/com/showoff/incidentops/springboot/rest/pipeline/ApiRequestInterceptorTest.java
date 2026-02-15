package com.showoff.incidentops.springboot.rest.pipeline;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ApiRequestInterceptorTest {
    @Test
    void interceptor_setsStartTimeAndProcessingHeader() {
        ApiRequestInterceptor interceptor = new ApiRequestInterceptor();
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v2/incidents/INC-1");
        MockHttpServletResponse response = new MockHttpServletResponse();

        assertTrue(interceptor.preHandle(request, response, new Object()));
        assertTrue(request.getAttribute("incidentops.startNanos") instanceof Long);

        interceptor.afterCompletion(request, response, new Object(), null);
        assertNotNull(response.getHeader("X-Processing-Time-Ms"));
    }

    @Test
    void interceptor_skipsHeaderWhenStartTimeMissing() {
        ApiRequestInterceptor interceptor = new ApiRequestInterceptor();
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v2/incidents/INC-2");
        MockHttpServletResponse response = new MockHttpServletResponse();

        interceptor.afterCompletion(request, response, new Object(), null);
        assertTrue(response.getHeader("X-Processing-Time-Ms") == null);
    }
}
