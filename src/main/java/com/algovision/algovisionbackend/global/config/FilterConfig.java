package com.algovision.algovisionbackend.global.config;

import com.algovision.algovisionbackend.global.logging.ApiLoggingFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

@Configuration
public class FilterConfig {

    @Bean
    public FilterRegistrationBean<ApiLoggingFilter> apiLoggingFilter(@Value("${logging.slow-request-threshold:1000}") long threshold){
        ApiLoggingFilter filter = new ApiLoggingFilter();
        filter.setSlowRequestThreshold(threshold);

        FilterRegistrationBean<ApiLoggingFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(filter);

        registration.setOrder(Ordered.HIGHEST_PRECEDENCE);

        return registration;
    }
}
