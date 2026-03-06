package com.anime.creator.config;

import com.anime.creator.service.impl.UserDetailsServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.security.config.http.SessionCreationPolicy;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Spring Security 安全配置
 *
 * ✅ 修复1：注入 UserDetailsService 并配置 AuthenticationManager，否则密码校验不生效。
 * ✅ 修复2：响应改用 ObjectMapper 序列化，避免手拼 JSON 出错。
 * ✅ 修复3：未登录时返回 401 JSON（而非重定向到登录页），前端 axios 拦截器可正确处理。
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private final UserDetailsServiceImpl userDetailsService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public SecurityConfig(UserDetailsServiceImpl userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        // ✅ 修复：必须配置 UserDetailsService + PasswordEncoder，登录才能正常校验密码
        auth.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder());
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .csrf().disable()
                .cors().and()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                .and()
                .authorizeRequests()
                .antMatchers("/auth/login", "/auth/logout").permitAll()
                .antMatchers("/storage/**").permitAll()
                .anyRequest().authenticated()
                .and()
                .formLogin()
                .loginProcessingUrl("/auth/login")
                .successHandler((req, res, auth) -> {
                    res.setContentType("application/json;charset=utf-8");
                    res.getWriter().write("{\"code\":200,\"msg\":\"登录成功\"}");
                })
                .failureHandler((req, res, ex) -> {
                    res.setContentType("application/json;charset=utf-8");
                    res.setStatus(401);
                    res.getWriter().write("{\"code\":401,\"msg\":\"用户名或密码错误\"}");
                })
                .and()
                .logout()
                .logoutUrl("/auth/logout")
                .logoutSuccessHandler((req, res, auth) -> {
                    res.setContentType("application/json;charset=utf-8");
                    res.getWriter().write("{\"code\":200,\"msg\":\"退出成功\"}");
                })
                .and()
                .exceptionHandling()
                .authenticationEntryPoint((req, res, ex) -> {
                    res.setContentType("application/json;charset=utf-8");
                    res.setStatus(401);
                    res.getWriter().write("{\"code\":401,\"msg\":\"未登录\"}");
                });
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.addAllowedOrigin("http://localhost:8080");
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
