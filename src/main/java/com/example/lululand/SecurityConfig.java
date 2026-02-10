package com.example.lululand;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.web.cors.*;

import java.util.List;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(Customizer.withDefaults())
            .csrf(csrf -> csrf.disable())

            .authorizeHttpRequests(auth -> auth
            	    .requestMatchers(org.springframework.http.HttpMethod.OPTIONS, "/**").permitAll()

            	    .requestMatchers(
            	        "/",
            	        "/index",
            	        "/login",
            	        "/signup",
            	        "/api/signup",
            	        "/api/login",
            	        "/api/hello",
            	        "/api/find-id",
            	        "/api/find-password",
            	        "/css/**",
            	        "/js/**",
            	        "/images/**"
            	    ).permitAll()

            	    .anyRequest().authenticated()
            	)

            .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .httpBasic(b -> b.disable());

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration config = new CorsConfiguration();

        // ✅ 정확한 Origin 지정
        config.setAllowedOrigins(List.of(
            "https://lululand.co.kr",
            "https://www.lululand.co.kr"
        ));

        config.setAllowedMethods(List.of(
            "GET","POST","PUT","DELETE","OPTIONS"
        ));

        config.setAllowedHeaders(List.of("*"));

        // ✅ 이거 꼭 true
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source =
            new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration("/**", config);

        return source;
    }
}
