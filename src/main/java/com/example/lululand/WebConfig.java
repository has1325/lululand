package com.example.lululand;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
  @Override
  public void addCorsMappings(CorsRegistry registry) {
    registry.addMapping("/api/**")
      .allowedOrigins("https://lululand.co.kr", "https://gem-color-counseling-yuhaseong1300.replit.app/api/consult") // 프론트 도메인으로 변경
      .allowedMethods("GET","POST","PUT","DELETE","OPTIONS")
      .allowedHeaders("*")
      .allowCredentials(true);
  }
}
