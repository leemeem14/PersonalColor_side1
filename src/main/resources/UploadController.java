package kr.ac.kopo.lyh.personalcolor.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import kr.ac.kopo.lyh.personalcolor.entity.ColorAnalysis;
import kr.ac.kopo.lyh.personalcolor.entity.User;
import kr.ac.kopo.lyh.personalcolor.service.ColorAnalysisService;
import kr.ac.kopo.lyh.personalcolor.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Controller
@RequiredArgsConstructor
@Slf4j
public class UploadController {

    private final FileStorageService fileStorageService;
    private final ColorAnalysisService colorAnalysisService;

    @GetMapping("/")
    public String index() {
        return "index"; // index.html로 연결
    }

    @PostMapping("/upload")
    @ResponseBody
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file,
                                        HttpServletRequest request) {
        try {
            // 로그인 확인 (없으면 익명도 허용, 필요시 아래 주석 해제)
            /*
            HttpSession session = request.getSession(false);
            if (session == null || session.getAttribute("user") == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("success", false, "error", "로그인이 필요합니다."));
            }
            User user = (User) session.getAttribute("user");
            */
            User user = null; // 로그인 없이 사용 가능하게

            // 파일 검증
            if (file.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "error", "파일이 비어있습니다."));
            }

            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "error", "이미지 파일만 업로드 가능합니다."));
            }

            // 파일 저장
            String storedFileName = fileStorageService.storeFile(file);

            // 이미지 분석 수행
            ColorAnalysis analysis = colorAnalysisService.analyzeImage(
                    user,
                    file.getOriginalFilename(),
                    storedFileName
            );

            // 세션에 분석 결과 ID 저장 (로그인 없이 사용시 세션 생성)
            HttpSession session = request.getSession(true);
            session.setAttribute("latestAnalysisId", analysis.getId());

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "분석이 완료되었습니다!",
                    "analysisId", analysis.getId(),
                    "redirectUrl", "/results"
            ));

        } catch (Exception e) {
            log.error("파일 업로드 및 분석 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "error", "분석 중 오류가 발생했습니다."));
        }
    }

    @GetMapping("/results")
    public String results(HttpServletRequest request, Model model) {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("latestAnalysisId") == null) {
            return "redirect:/";
        }

        Long analysisId = (Long) session.getAttribute("latestAnalysisId");
        ColorAnalysis analysis = colorAnalysisService.getAnalysisById(analysisId);

        if (analysis == null) {
            return "redirect:/";
        }

        model.addAttribute("analysis", analysis);
        model.addAttribute("colorType", analysis.getColorType().getDisplayName());
        model.addAttribute("description", analysis.getDescription());
        model.addAttribute("confidence", Math.round(analysis.getConfidence() * 100));
        model.addAttribute("uploadedFile", analysis.getStoredFileName());

        return "results"; // results.html로 결과 표시
    }
}
