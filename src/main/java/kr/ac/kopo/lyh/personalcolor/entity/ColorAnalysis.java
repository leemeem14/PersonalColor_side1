package kr.ac.kopo.lyh.personalcolor.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "color_analysis")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ColorAnalysis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String originalFileName;

    @Column(nullable = false)
    private String storedFileName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ColorType colorType;

    @Column(length = 1000)
    private String description;

    // 분석 결과 상세 정보
    @Column
    private String dominantColors; // JSON 형태로 저장

    @Column
    private Float confidence; // 분석 신뢰도 (0.0 ~ 1.0)

    @CreationTimestamp
    private LocalDateTime analyzedAt;

    public enum ColorType {
        SPRING_WARM("봄 웜톤", "따뜻하고 생기있는 색상"),
        SUMMER_COOL("여름 쿨톤", "시원하고 우아한 색상"),
        AUTUMN_WARM("가을 웜톤", "깊고 따뜻한 색상"),
        WINTER_COOL("겨울 쿨톤", "선명하고 차가운 색상");

        private final String displayName;
        private final String description;

        ColorType(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getDescription() {
            return description;
        }
    }
}