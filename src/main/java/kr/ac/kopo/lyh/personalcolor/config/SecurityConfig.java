package kr.ac.kopo.lyh.personalcolor.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 권한 관리
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers("/", "/login", "/signup", "/api/login", "/api/signup").permitAll()
                        .requestMatchers("/css/**", "/static/js/**", "/images/**", "/static/**").permitAll()
                        .requestMatchers("/api/user/current").permitAll() // 로그인 상태 확인 API
                        .requestMatchers("/upload", "/api/logout").authenticated() // 업로드는 로그인 필요
                        .anyRequest().authenticated()
                )
                // 폼 로그인 설정
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login") // Spring Security 기본 로그인 처리 URL
                        .defaultSuccessUrl("/upload", true)
                        .failureUrl("/login?error=true")
                        .permitAll()
                )
                // 로그아웃 설정 개선
                .logout(logout -> logout
                        .logoutUrl("/logout") // GET 요청도 허용
                        .logoutSuccessUrl("/")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .clearAuthentication(true)
                        .permitAll()
                        // 로그아웃 성공 핸들러 추가
                        .logoutSuccessHandler((request, response, authentication) -> {
                            // AJAX 요청인지 확인
                            String requestedWith = request.getHeader("X-Requested-With");
                            if ("XMLHttpRequest".equals(requestedWith)) {
                                response.setContentType("application/json;charset=UTF-8");
                                response.getWriter().write("{\"success\":true,\"redirectUrl\":\"/\"}");
                            } else {
                                response.sendRedirect("/");
                            }
                        })
                )
                // CSRF 설정
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers("/api/**") // API 엔드포인트는 CSRF 비활성화
                )
                // 세션 관리 설정
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                        .maximumSessions(1)
                        .maxSessionsPreventsLogin(false)
                        .expiredUrl("/login?expired=true")
                )
                // 예외 처리
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint((request, response, authException) -> {
                            String requestedWith = request.getHeader("X-Requested-With");
                            if ("XMLHttpRequest".equals(requestedWith)) {
                                response.setStatus(401);
                                response.setContentType("application/json;charset=UTF-8");
                                response.getWriter().write("{\"success\":false,\"error\":\"로그인이 필요합니다.\",\"redirectUrl\":\"/login\"}");
                            } else {
                                response.sendRedirect("/login");
                            }
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            String requestedWith = request.getHeader("X-Requested-With");
                            if ("XMLHttpRequest".equals(requestedWith)) {
                                response.setStatus(403);
                                response.setContentType("application/json;charset=UTF-8");
                                response.getWriter().write("{\"success\":false,\"error\":\"접근 권한이 없습니다.\"}");
                            } else {
                                response.sendRedirect("/login");
                            }
                        })
                )
                // 보안 헤더 설정
                .headers(headers -> headers
                        .frameOptions().deny()
                        .contentTypeOptions().and()
                        .httpStrictTransportSecurity(hstsConfig -> hstsConfig
                                .maxAgeInSeconds(31536000)
                                .includeSubdomains(true)
                        )
                        .referrerPolicy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN)
                );

        return http.build();
    }
}