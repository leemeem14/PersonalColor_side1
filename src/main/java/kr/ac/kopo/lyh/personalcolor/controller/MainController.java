package kr.ac.kopo.lyh.personalcolor.controller;

import kr.ac.kopo.lyh.personalcolor.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

@Controller
@Slf4j
public class MainController {

    @GetMapping("/")
    public String index(HttpServletRequest request, Model model) {
        log.info("메인 페이지 접근");

        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute("user") != null) {
            User user = (User) session.getAttribute("user");
            model.addAttribute("user", user);
            model.addAttribute("isLoggedIn", true);
        } else {
            model.addAttribute("isLoggedIn", false);
        }

        return "index";
    }

    @GetMapping("/login")
    public String login(@RequestParam(value = "error", required = false) String error,
                        @RequestParam(value = "logout", required = false) String logout,
                        Model model, HttpServletRequest request) {
        log.info("로그인 페이지 접근");

        // 이미 로그인된 사용자는 메인 페이지로 리다이렉트
        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute("user") != null) {
            return "redirect:/";
        }

        if (error != null) {
            model.addAttribute("error", "로그인에 실패했습니다. 이메일과 비밀번호를 확인해주세요.");
        }

        if (logout != null) {
            model.addAttribute("message", "로그아웃되었습니다.");
        }

        if ("access-denied".equals(error)) {
            model.addAttribute("error", "접근 권한이 없습니다. 로그인해주세요.");
        }

        return "login";
    }

    @GetMapping("/register")
    public String register(HttpServletRequest request) {
        log.info("회원가입 페이지 접근");

        // 이미 로그인된 사용자는 메인 페이지로 리다이렉트
        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute("user") != null) {
            return "redirect:/";
        }

        return "register";
    }

    @GetMapping("/upload")
    public String upload(HttpServletRequest request, Model model) {
        log.info("업로드 페이지 접근");

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            log.warn("로그인하지 않은 사용자가 업로드 페이지에 접근 시도");
            return "redirect:/login";
        }

        User user = (User) session.getAttribute("user");
        model.addAttribute("user", user);

        return "upload";
    }

    // favicon.ico 요청 처리
    @GetMapping("/favicon.ico")
    @ResponseBody
    public ResponseEntity<Void> favicon() {
        log.debug("favicon.ico 요청 처리");
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    // 인증 상태 확인 API
    @GetMapping("/api/auth/status")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getAuthStatus(HttpServletRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAuthenticated = auth != null && auth.isAuthenticated() &&
                !"anonymousUser".equals(auth.getPrincipal());

        HttpSession session = request.getSession(false);
        Object user = session != null ? session.getAttribute("user") : null;

        Map<String, Object> response = new HashMap<>();
        response.put("isAuthenticated", isAuthenticated);
        response.put("hasUser", user != null);

        if (user != null) {
            User userObj = (User) user;
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("id", userObj.getId());
            userInfo.put("email", userObj.getEmail());
            userInfo.put("name", userObj.getName());
            response.put("user", userInfo);
        }

        return ResponseEntity.ok(response);
    }

    // 헬스체크 엔드포인트
    @GetMapping("/api/health")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("timestamp", System.currentTimeMillis());
        response.put("service", "PersonalColor Application");

        return ResponseEntity.ok(response);
    }
}