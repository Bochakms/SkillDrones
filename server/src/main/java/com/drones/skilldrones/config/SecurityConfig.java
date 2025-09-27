package com.drones.skilldrones.config;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
//import org.springframework.security.web.SecurityFilterChain;

//import static org.springframework.security.config.Customizer.withDefaults;

//@Configuration
public class SecurityConfig {
//
//    @Bean
//    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
//        http
//                .authorizeHttpRequests(authz -> authz
//                        .requestMatchers("/api/**").permitAll() // Разрешить все API-запросы
//                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll() // Разрешить Swagger
//                        .anyRequest().authenticated() // Все остальное пока требует аутентификации
//                )
//                .csrf(AbstractHttpConfigurer::disable); // Отключить CSRF для тестирования
//
//        return http.build();
//    }
}
//authenticated()