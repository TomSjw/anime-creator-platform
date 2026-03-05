package com.anime.creator.config;

import com.anime.creator.service.impl.UserDetailsServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

/**
 * Spring Security 安全配置
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private final UserDetailsServiceImpl userDetailsService;

    public SecurityConfig(UserDetailsServiceImpl userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            // 关闭CSRF
            .csrf().disable()
            // 开启跨域
            .cors().and()
            // 会话管理
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED).and()
            // 请求授权
            .authorizeRequests()
            // 登录接口放开
            .antMatchers("/auth/login").permitAll()
            // 静态资源放开
            .antMatchers("/storage/**").permitAll()
            // 其他所有接口都需要认证
            .anyRequest().authenticated()
            .and()
            // 登录配置
            .formLogin()
            .loginProcessingUrl("/auth/login")
            .successHandler((request, response, authentication) -> {
                response.setContentType("application/json;charset=utf-8");
                response.getWriter().write("{\"code\":200,\"msg\":\"登录成功\",\"data\":\"登录成功\"}");
            })
            .failureHandler((request, response, exception) -> {
                response.setContentType("application/json;charset=utf-8");
                response.getWriter().write("{\"code\":500,\"msg\":\"用户名或密码错误\"}");
            })
            .and()
            .logout()
            .logoutUrl("/auth/logout")
            .logoutSuccessHandler((request, response, authentication) -> {
                response.setContentType("application/json;charset=utf-8");
                response.getWriter().write("{\"code\":200,\"msg\":\"退出成功\"}");
            });
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("*"));
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
