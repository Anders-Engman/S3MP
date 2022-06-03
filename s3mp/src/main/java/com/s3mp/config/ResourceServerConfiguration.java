package com.s3mp.config;

// ResourceServerConfiguration.java
// Author: Anders Engman
// Date: 6/3/22
// This configures the RESTful api endpoint security, determining which endpoints require authentication and which
// do not. In this case, only one endpoint detailed below does not require authentication.

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;

@Configuration
@EnableResourceServer
public class ResourceServerConfiguration extends ResourceServerConfigurerAdapter {

    public ResourceServerConfiguration() { }

    @Override
    public void configure(ResourceServerSecurityConfigurer resources) {
        resources.resourceId("api");
    }

    @Override
    public void configure(HttpSecurity http) throws Exception {
        http
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .antMatcher("/**")
            .authorizeRequests()
            // V1/check is the only endpoint that can be used without authentication
            // All other requests require authentication
            .antMatchers("/v1/check**").permitAll()
            .anyRequest().authenticated();
    }

}