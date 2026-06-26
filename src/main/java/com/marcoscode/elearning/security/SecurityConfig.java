package com.marcoscode.elearning.security;

import com.marcoscode.elearning.exception.CustomAccessDeniedHandle;
import com.marcoscode.elearning.exception.CustomAuthenticationEntryPoint;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CustomAuthenticationEntryPoint authenticationEntryPoint;
    private final CustomAccessDeniedHandle accessDeniedHandler;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return  new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config
    ) {
        return config.getAuthenticationManager();
    }

    @Bean
    SecurityFilterChain securityFilterChain(
            HttpSecurity http
    ) {
        http
                .csrf(
                        AbstractHttpConfigurer::disable
                )
                .sessionManagement(
                        session -> session
                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler)
                )
                .exceptionHandling(
                        exception -> exception
                                .authenticationEntryPoint(
                                        authenticationEntryPoint
                                )
                )
                .authorizeHttpRequests(
                        auth -> auth
                                .requestMatchers("/v3/api-docs/**", "/swagger-ui/**",
                                        "/swagger-ui.html").permitAll()
                                .requestMatchers("/api/v1/auth/**", "/error").permitAll()
                                .requestMatchers(HttpMethod.POST, "/api/v1/students").permitAll()
                                .requestMatchers(HttpMethod.GET, "/api/v1/courses/**").permitAll()
                                .requestMatchers(HttpMethod.GET, "/api/v1/sections/**").permitAll()
                                .requestMatchers(HttpMethod.GET, "/api/v1/lectures/**").permitAll()
                                .requestMatchers(HttpMethod.GET, "/api/v1/instructors/**").permitAll()
                                .requestMatchers(HttpMethod.POST, "/api/v1/courses").hasAnyRole(
                                        "INSTRUCTOR",
                                        "ADMIN")
                                .requestMatchers(HttpMethod.POST, "/api/v1/enrollments").hasAnyRole(
                                        "STUDENT",
                                        "ADMIN")
                                .requestMatchers(HttpMethod.POST, "/api/v1/instructors").hasRole("ADMIN")

                                .anyRequest().authenticated()
                )
                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }


}