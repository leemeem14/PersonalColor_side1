package kr.ac.kopo.lyh.personalcolor.config;

import jakarta.servlet.MultipartConfigElement;
import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.unit.DataSize;

@Configuration
public class ApplicationConfig {

    @Bean
    public MultipartConfigElement multipartConfigElement() {
        MultipartConfigFactory factory = new MultipartConfigFactory();
        factory.setMaxFileSize(DataSize.ofMegabytes(10)); // 10MB
        factory.setMaxRequestSize(DataSize.ofMegabytes(10)); // 10MB
        return factory.createMultipartConfig();
    }

    // CommonsMultipartResolver 제거 - Spring Boot 기본 multipart 지원 사용
    // application.properties에서 다음 설정으로 대체:
    // spring.servlet.multipart.max-file-size=10MB
    // spring.servlet.multipart.max-request-size=10MB
}