package com.poomaalai.configuration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig{

    @Autowired
    private UserDetailsService creatorService;

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
 
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(authorizeRequests -> authorizeRequests
                .requestMatchers(
                    "/", 
                    "/error",
                    "/search",
                    "/register",
                    "/logout",
                    "/creator/register",
                    "/creator/login",
                    "/creator/logout",
                    "/creator-store/search",
                    "/creator-store/add",
                    "/favicon.ico",
                    "/css/**",
                    "/js/**",
                    "/images/**",
                    "/styles.css"
                ).permitAll()
                .anyRequest().authenticated()
            )
            .formLogin(formLogin -> formLogin
                .loginPage("/creator/login")
                .loginProcessingUrl("/creator/login") 
                .defaultSuccessUrl("/creator/dashboard")
                .permitAll()
            )
            .logout(logout -> 
                logout
                .logoutUrl("/creator/logout")
                .logoutSuccessUrl("/creator/login?logout")
                .permitAll()
            )
            .authenticationProvider(authenticationProvider());
        return http.build();
    }


   @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(creatorService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

}