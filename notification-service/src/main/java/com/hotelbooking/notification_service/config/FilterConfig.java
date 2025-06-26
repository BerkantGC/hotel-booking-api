package com.hotelbooking.notification_service.config;

import com.hotelbooking.notification_service.util.InternalAuthFilter;
import com.hotelbooking.notification_service.util.RoleAuthenticationFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FilterConfig {
    @Bean
    public FilterRegistrationBean<InternalAuthFilter> internalFilter(InternalAuthFilter filter) {
        FilterRegistrationBean<InternalAuthFilter> bean = new FilterRegistrationBean<>(filter);
        bean.addUrlPatterns("/api/v1/notifications/*");
        bean.setOrder(1);
        return bean;
    }

    @Bean
    public FilterRegistrationBean<RoleAuthenticationFilter> authFilter(RoleAuthenticationFilter filter) {
        FilterRegistrationBean<RoleAuthenticationFilter> bean = new FilterRegistrationBean<>(filter);
        bean.addUrlPatterns("/api/v1/notifications/*");
        bean.setOrder(2);
        return bean;
    }
}
