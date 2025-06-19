package kr.ac.kopo.lyh.personalcolor.controller;

import org.springframework.ui.Model;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@Slf4j
public class MainController {

    @GetMapping("/")
    public String home() {
        return "index";
    }

    @GetMapping("/home")
    public String homeRedirect() {
        // /home 요청을 메인 페이지로 리다이렉트
        return "redirect:/";
    }

    @GetMapping("/upload")
    public String upload() {
        // Spring Security를 통한 인증 체크는 SecurityConfig에서 처리
        // 인증되지 않은 사용자는 자동으로 로그인 페이지로 리다이렉트됨
        log.info("업로드 페이지 접근");
        return "upload";
    }

    @GetMapping("/menu")
    public String menu() {
        return "redirect:/shop";
    }

    @GetMapping("/shop")
    public String shop() {
        return "shop";
    }

    // 현재 로그인 상태 확인을 위한 API 엔드포인트
    @GetMapping("/api/auth/status")
    @ResponseBody
    public ResponseEntity<?> getAuthStatus(HttpServletRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAuthenticated = auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal());

        HttpSession session = request.getSession(false);
        Object user = session != null ? session.getAttribute("user") : null;

        Map<String, Object> response = new HashMap<>();
        response.put("isAuthenticated", isAuthenticated);
        response.put("hasUser", user != null);

        return ResponseEntity.ok(response);
    }
}
