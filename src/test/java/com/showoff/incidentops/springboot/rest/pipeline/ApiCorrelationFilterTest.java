package com.showoff.incidentops.springboot.rest.pipeline;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ApiCorrelationFilterTest {
    @Test
    void filter_usesIncomingCorrelationIdWhenProvided() throws Exception {
        ApiCorrelationFilter filter = new ApiCorrelationFilter();
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v2/incidents/INC-1");
        request.addHeader(ApiCorrelationFilter.CORRELATION_ID_HEADER, " req-123 ");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, new MockFilterChain());

        assertEquals("req-123", request.getAttribute(ApiCorrelationFilter.CORRELATION_ID_ATTRIBUTE));
        assertEquals("req-123", response.getHeader(ApiCorrelationFilter.CORRELATION_ID_HEADER));
    }

    @Test
    void filter_generatesCorrelationIdWhenMissingOrBlank() throws Exception {
        ApiCorrelationFilter filter = new ApiCorrelationFilter();

        MockHttpServletRequest missingHeaderRequest = new MockHttpServletRequest("GET", "/api/v2/incidents/INC-1");
        MockHttpServletResponse missingHeaderResponse = new MockHttpServletResponse();
        filter.doFilter(missingHeaderRequest, missingHeaderResponse, new MockFilterChain());
        String generated = (String) missingHeaderRequest.getAttribute(ApiCorrelationFilter.CORRELATION_ID_ATTRIBUTE);
        assertNotNull(generated);
        assertEquals(generated, missingHeaderResponse.getHeader(ApiCorrelationFilter.CORRELATION_ID_HEADER));
        assertTrue(generated.length() >= 32);

        MockHttpServletRequest blankHeaderRequest = new MockHttpServletRequest("GET", "/api/v2/incidents/INC-2");
        blankHeaderRequest.addHeader(ApiCorrelationFilter.CORRELATION_ID_HEADER, " ");
        MockHttpServletResponse blankHeaderResponse = new MockHttpServletResponse();
        filter.doFilter(blankHeaderRequest, blankHeaderResponse, new MockFilterChain());
        String generatedBlank = (String) blankHeaderRequest.getAttribute(ApiCorrelationFilter.CORRELATION_ID_ATTRIBUTE);
        assertNotNull(generatedBlank);
        assertEquals(generatedBlank, blankHeaderResponse.getHeader(ApiCorrelationFilter.CORRELATION_ID_HEADER));
        assertTrue(generatedBlank.length() >= 32);
    }
}
