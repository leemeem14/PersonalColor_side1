package kr.ac.kopo.lyh.personalcolor.controller;

import kr.ac.kopo.lyh.personalcolor.entity.ColorAnalysis;
import kr.ac.kopo.lyh.personalcolor.entity.User;
import kr.ac.kopo.lyh.personalcolor.service.ColorAnalysisService;
import kr.ac.kopo.lyh.personalcolor.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@Slf4j
public class UploadController {

    private final FileStorageService fileStorageService;
    private final ColorAnalysisService colorAnalysisService;

    // 허용되는 이미지 MIME 타입
    private static final List<String> ALLOWED_CONTENT_TYPES = Arrays.asList(
            "image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp"
    );

    // 최대 파일 크기 (10MB)
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;

    @PostMapping("/api/upload")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> uploadFile(
            @RequestParam("file") MultipartFile file,
            HttpServletRequest request) {

        log.info("파일 업로드 요청 시작: {}", file.getOriginalFilename());

        try {
            // 로그인 확인
            HttpSession session = request.getSession(false);
            if (session == null || session.getAttribute("user") == null) {
                log.warn("로그인하지 않은 사용자의 파일 업로드 시도");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("success", false, "error", "로그인이 필요합니다."));
            }

            User user = (User) session.getAttribute("user");
            log.info("사용자 {} 파일 업로드 시도", user.getEmail());

            // 파일 기본 검증
            if (file.isEmpty()) {
                log.warn("빈 파일 업로드 시도");
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "error", "파일이 비어있습니다."));
            }

            // 파일 크기 검증
            if (file.getSize() > MAX_FILE_SIZE) {
                log.warn("파일 크기 초과: {} bytes", file.getSize());
                return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                        .body(Map.of("success", false, "error", "파일 크기가 너무 큽니다. (최대 10MB)"));
            }

            // 이미지 파일 확인
            String contentType = file.getContentType();
            if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
                log.warn("지원하지 않는 파일 형식: {}", contentType);
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "error",
                                "지원하지 않는 파일 형식입니다. (JPEG, PNG, GIF, WebP만 지원)"));
            }

            // 파일명 검증
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || originalFilename.trim().isEmpty()) {
                log.warn("파일명이 없는 파일 업로드 시도");
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "error", "올바른 파일명이 필요합니다."));
            }

            // 파일 저장
            log.info("파일 저장 시작: {}", originalFilename);
            String storedFileName = fileStorageService.storeFile(file);
            log.info("파일 저장 완료: {} -> {}", originalFilename, storedFileName);

            // 이미지 분석 수행
            log.info("이미지 분석 시작");
            ColorAnalysis analysis = colorAnalysisService.analyzeImage(
                    user,
                    originalFilename,
                    storedFileName
            );
            log.info("이미지 분석 완료: ID {}", analysis.getId());

            // 세션에 분석 결과 ID 저장
            session.setAttribute("latestAnalysisId", analysis.getId());

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "분석이 완료되었습니다!",
                    "analysisId", analysis.getId(),
                    "redirectUrl", "/results",
                    "colorType", analysis.getColorType().getDisplayName(),
                    "confidence", Math.round(analysis.getConfidence() * 100)
            ));

        } catch (IllegalArgumentException e) {
            log.error("잘못된 파라미터로 인한 파일 업로드 실패: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "error", e.getMessage()));

        } catch (Exception e) {
            log.error("파일 업로드 및 분석 중 예상치 못한 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "error", "분석 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요."));
        }
    }

    @GetMapping("/results")
    public String results(HttpServletRequest request, Model model) {
        log.info("결과 페이지 접근");

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            log.warn("로그인하지 않은 사용자의 결과 페이지 접근 시도");
            return "redirect:/login";
        }

        User user = (User) session.getAttribute("user");
        Long analysisId = (Long) session.getAttribute("latestAnalysisId");

        ColorAnalysis analysis;
        if (analysisId != null) {
            // 특정 분석 결과 조회
            analysis = colorAnalysisService.getUserAnalyses(user).stream()
                    .filter(a -> a.getId().equals(analysisId))
                    .findFirst()
                    .orElse(null);
        } else {
            // 최근 분석 결과 조회
            analysis = colorAnalysisService.getLatestAnalysis(user);
        }

        if (analysis == null) {
            log.warn("사용자 {}의 분석 결과를 찾을 수 없음", user.getEmail());
            return "redirect:/upload?error=no-analysis";
        }

        log.info("분석 결과 표시: ID {}, 사용자 {}", analysis.getId(), user.getEmail());

        model.addAttribute("user", user);
        model.addAttribute("analysis", analysis);
        model.addAttribute("colorType", analysis.getColorType().getDisplayName());
        model.addAttribute("description", analysis.getDescription());
        model.addAttribute("confidence", Math.round(analysis.getConfidence() * 100));
        model.addAttribute("uploadedFile", analysis.getStoredFileName());

        return "results";
    }

    @GetMapping("/history")
    public String history(HttpServletRequest request,
                          @RequestParam(defaultValue = "0") int page,
                          @RequestParam(defaultValue = "10") int size,
                          Model model) {
        log.info("히스토리 페이지 접근");

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            log.warn("로그인하지 않은 사용자의 히스토리 페이지 접근 시도");
            return "redirect:/login";
        }

        User user = (User) session.getAttribute("user");
        Pageable pageable = PageRequest.of(page, size);
        Page<ColorAnalysis> analyses = colorAnalysisService.getUserAnalyses(user, pageable);

        log.info("사용자 {} 히스토리 조회: {} 건", user.getEmail(), analyses.getTotalElements());

        model.addAttribute("user", user);
        model.addAttribute("analyses", analyses);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", analyses.getTotalPages());
        model.addAttribute("totalElements", analyses.getTotalElements());

        return "history";
    }

    @DeleteMapping("/api/analysis/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteAnalysis(
            @PathVariable Long id,
            HttpServletRequest request) {

        log.info("분석 결과 삭제 요청: ID {}", id);

        try {
            HttpSession session = request.getSession(false);
            if (session == null || session.getAttribute("user") == null) {
                log.warn("로그인하지 않은 사용자의 분석 결과 삭제 시도");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("success", false, "error", "로그인이 필요합니다."));
            }

            User user = (User) session.getAttribute("user");
            colorAnalysisService.deleteAnalysis(id, user);

            log.info("분석 결과 삭제 완료: ID {}, 사용자 {}", id, user.getEmail());

            return ResponseEntity.ok(Map.of("success", true, "message", "분석 결과가 삭제되었습니다."));

        } catch (IllegalArgumentException e) {
            log.warn("분석 결과 삭제 실패 - 권한 없음: ID {}, 오류: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("success", false, "error", e.getMessage()));

        } catch (Exception e) {
            log.error("분석 결과 삭제 중 오류 발생: ID {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "error", "삭제 중 오류가 발생했습니다."));
        }
    }
}