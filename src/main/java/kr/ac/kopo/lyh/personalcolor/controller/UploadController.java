package kr.ac.kopo.lyh.personalcolor.controller;

import ch.qos.logback.core.model.Model;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.Random;

@Controller
@RequiredArgsConstructor
@Slf4j
public class UploadController {

    private final FileStorageService fileStorageService;

    @GetMapping("/upload")
    public String uploadForm() {
        return "upload";
    }

    @PostMapping("/upload")
    @ResponseBody
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file,
                                        HttpServletRequest request) {
        try {
            // 로그인 확인
            HttpSession session = request.getSession(false);
            if (session == null || session.getAttribute("user") == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("success", false, "error", "로그인이 필요합니다."));
            }

            // 파일 검증
            if (file.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "error", "파일이 비어있습니다."));
            }

            // 이미지 파일 확인
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "error", "이미지 파일만 업로드 가능합니다."));
            }

            // 파일 저장
            String fileName = fileStorageService.storeFile(file);

            // 세션에 업로드된 파일 정보 저장
            session.setAttribute("uploadedFile", fileName);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "파일 업로드 성공",
                    "fileName", fileName
            ));

        } catch (Exception e) {
            log.error("파일 업로드 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "error", "파일 업로드 중 오류가 발생했습니다."));
        }
    }

    @GetMapping("/results")
    public String results(HttpServletRequest request, Model model) {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            return "redirect:/login";
        }

        // 업로드된 파일이 있는지 확인
        String uploadedFile = (String) session.getAttribute("uploadedFile");
        if (uploadedFile == null) {
            return "redirect:/upload";
        }

        // 임시로 랜덤 결과 생성 (나중에 실제 분석 결과로 대체)
        String[] colorTypes = {"봄 웜톤", "여름 쿨톤", "가을 웜톤", "겨울 쿨톤"};
        String[] descriptions = {
                "따뜻하고 생기있는 색상이 잘 어울립니다!",
                "시원하고 우아한 색상이 잘 어울립니다!",
                "깊고 따뜻한 색상이 잘 어울립니다!",
                "선명하고 차가운 색상이 잘 어울립니다!"
        };

        Random random = new Random();
        int index = random.nextInt(colorTypes.length);

        model.addAttribute("colorType", colorTypes[index]);
        model.addAttribute("description", descriptions[index]);
        model.addAttribute("uploadedFile", uploadedFile);

        return "results";
    }
}
