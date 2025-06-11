package kr.ac.kopo.lyh.personalcolor.repository;

import kr.ac.kopo.lyh.personalcolor.entity.ColorAnalysis;
import kr.ac.kopo.lyh.personalcolor.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ColorAnalysisRepository extends JpaRepository<ColorAnalysis, Long> {

    // 사용자별 분석 결과 조회
    List<ColorAnalysis> findByUserOrderByAnalyzedAtDesc(User user);

    // 사용자별 분석 결과 페이징 조회
    Page<ColorAnalysis> findByUserOrderByAnalyzedAtDesc(User user, Pageable pageable);

    // 사용자의 최근 분석 결과 조회
    Optional<ColorAnalysis> findFirstByUserOrderByAnalyzedAtDesc(User user);

    // 특정 기간 내 분석 결과 조회
    @Query("SELECT ca FROM ColorAnalysis ca WHERE ca.user = :user AND ca.analyzedAt BETWEEN :startDate AND :endDate ORDER BY ca.analyzedAt DESC")
    List<ColorAnalysis> findByUserAndDateRange(@Param("user") User user,
                                               @Param("startDate") LocalDateTime startDate,
                                               @Param("endDate") LocalDateTime endDate);

    // 컬러 타입별 통계
    @Query("SELECT ca.colorType, COUNT(ca) FROM ColorAnalysis ca GROUP BY ca.colorType")
    List<Object[]> getColorTypeStatistics();

    // 사용자별 분석 횟수
    @Query("SELECT COUNT(ca) FROM ColorAnalysis ca WHERE ca.user = :user")
    Long countByUser(@Param("user") User user);
}