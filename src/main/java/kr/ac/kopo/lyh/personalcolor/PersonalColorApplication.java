package kr.ac.kopo.lyh.personalcolor;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class PersonalColorApplication {

    public static void main(String[] args) {
        SpringApplication.run(PersonalColorApplication.class, args);
    }

    @Bean
    CommandLineRunner init() {
        return args -> {
            System.out.println("========================================");
            System.out.println("🎨 퍼스널 컬러 애플리케이션이 시작되었습니다!");
            System.out.println("📱 http://localhost:8080 에서 확인하세요");
            System.out.println("========================================");
        };
    }
}
