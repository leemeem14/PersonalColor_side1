package kr.ac.kopo.lyh.personalcolor;

import kr.ac.kopo.lyh.personalcolor.entity.ColorAnalysis;
import kr.ac.kopo.lyh.personalcolor.entity.User;
import kr.ac.kopo.lyh.personalcolor.repository.ColorAnalysisRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ColorAnalysisService {

    private final ColorAnalysisRepository colorAnalysisRepository;
    private final Random random = new Random();

    /**
     * 이미지 분석 수행 (현재는 임시 랜덤 결과)
     * 추후 실제 AI 분석 로직으로 대체
     */
    public ColorAnalysis analyzeImage(User user, String originalFileName, String storedFileName) {
        // TODO: 실제 이미지 분석 로직 구현
        // 현재는 임시로 랜덤 결과 생성
        ColorAnalysis.ColorType[] colorTypes = ColorAnalysis.ColorType.values();
        ColorAnalysis.ColorType selectedType = colorTypes[random.nextInt(colorTypes.length)];

        ColorAnalysis analysis = ColorAnalysis.builder()
                .user(user)
                .originalFileName(originalFileName)
                .storedFileName(storedFileName)
                .colorType(selectedType)
                .description(selectedType.getDescription())
                .confidence(0.75f + random.nextFloat() * 0.25f) // 75-100% 신뢰도
                .dominantColors(generateSampleColors(selectedType))
                .build();

        ColorAnalysis savedAnalysis = colorAnalysisRepository.save(analysis);
        log.info("이미지 분석 완료: 사용자={}, 결과={}", user.getEmail(), selectedType.getDisplayName());

        return savedAnalysis;
    }

    /**
     * 사용자의 분석 결과 목록 조회
     */
    @Transactional(readOnly = true)
    public List<ColorAnalysis> getUserAnalyses(User user) {
        return colorAnalysisRepository.findByUserOrderByAnalyzedAtDesc(user);
    }

    /**
     * 사용자의 분석 결과 페이징 조회
     */
    @Transactional(readOnly = true)
    public Page<ColorAnalysis> getUserAnalyses(User user, Pageable pageable) {
        return colorAnalysisRepository.findByUserOrderByAnalyzedAtDesc(user, pageable);
    }

    /**
     * 사용자의 최근 분석 결과 조회
     */
    @Transactional(readOnly = true)
    public ColorAnalysis getLatestAnalysis(User user) {
        return colorAnalysisRepository.findFirstByUserOrderByAnalyzedAtDesc(user)
                .orElse(null);
    }

    /**
     * 분석 결과 삭제
     */
    public void deleteAnalysis(Long analysisId, User user) {
        ColorAnalysis analysis = colorAnalysisRepository.findById(analysisId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 분석 결과입니다."));

        if (!analysis.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("삭제 권한이 없습니다.");
        }

        colorAnalysisRepository.delete(analysis);
        log.info("분석 결과 삭제: ID={}, 사용자={}", analysisId, user.getEmail());
    }

    /**
     * 임시 색상 정보 생성 (실제 분석 결과로 대체 예정)
     */
    private String generateSampleColors(ColorAnalysis.ColorType colorType) {
        // JSON 형태로 대표 색상들 저장
        switch (colorType) {
            case SPRING_WARM:
                return "[\"#FFB6C1\", \"#FFA07A\", \"#F0E68C\", \"#98FB98\"]";
            case SUMMER_COOL:
                return "[\"#E6E6FA\", \"#B0C4DE\", \"#F0F8FF\", \"#DDA0DD\"]";
            case AUTUMN_WARM:
                return "[\"#D2691E\", \"#CD853F\", \"#B22222\", \"#8B4513\"]";
            case WINTER_COOL:
                return "[\"#000080\", \"#800080\", \"#DC143C\", \"#008B8B\"]";
            default:
                return "[\"#808080\"]";
        }
    }
}
