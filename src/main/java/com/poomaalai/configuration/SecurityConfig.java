package com.poomaalai.configuration;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.poomaalai.audit.ApplicationAuditAware;
import com.poomaalai.security.CustomAuthenticationEntryPoint;
import com.poomaalai.security.JwtAuthenticationFilter;
import com.poomaalai.security.JwtTokenProvider;
import com.poomaalai.security.RateLimitFilter;
import com.poomaalai.service.CreatorService;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig{

    @Autowired
    private CreatorService creatorService;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private RateLimitFilter rateLimitFilter;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private CustomAuthenticationEntryPoint customAuthenticationEntryPoint;
 
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .headers(headers -> headers
                .contentTypeOptions(contentType -> {})
                .frameOptions(frame -> frame.deny())
                .contentSecurityPolicy(csp -> csp.policyDirectives("default-src 'self'; script-src 'self' 'unsafe-inline'; style-src 'self' 'unsafe-inline'; img-src 'self' data:; font-src 'self'; connect-src 'self'; frame-ancestors 'none'; base-uri 'self'; form-action 'self'"))
                .httpStrictTransportSecurity(hsts -> hsts
                    .includeSubDomains(true)
                    .maxAgeInSeconds(31536000)
                )
            )
            .authorizeHttpRequests(authorizeRequests -> authorizeRequests
                .requestMatchers(
                    "/", 
                    "/error",
                    "/search",
                    "/register",
                    "/logout",
                    "/creator",
                    "/creator/{id}",
                    "/creator/api/register",
                    "/creator/register",
                    "/creator/login",
                    "/creator/api/login",
                    "/creator/logout",
                    "/creator-store/search",
                    "/favicon.ico",
                    "/css/**",
                    "/js/**",
                    "/images/**",
                    "/styles.css",
                    "/actuator/health"
                ).permitAll()
                .requestMatchers("/actuator/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .logout(logout -> 
                logout
                .logoutUrl("/creator/logout")
                .logoutSuccessUrl("/creator/login?logout")
                .permitAll()
            )
            .exceptionHandling(exception -> exception
                .authenticationEntryPoint(customAuthenticationEntryPoint)
            )
            .authenticationProvider(authenticationProvider())
            .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(rateLimitFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(jwtTokenProvider, (CreatorService) creatorService);
    }

   @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(creatorService);
        authProvider.setPasswordEncoder(passwordEncoder);
        return authProvider;
    }

        @Bean
        public AuditorAware<String> auditorAware(){
            return new ApplicationAuditAware();

        }
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:8080","https://www.poomaalai.com","https://poomaalai-8b5b97a1-production.up.railway.app","https://creator-production-8455.up.railway.app","https://api.poomaalai.com"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Content-Type", "Authorization"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

}