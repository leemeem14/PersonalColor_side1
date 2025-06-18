package kr.ac.kopo.lyh.personalcolor.controller;

import kr.ac.kopo.lyh.personalcolor.entity.User;
import kr.ac.kopo.lyh.personalcolor.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

/**
 * 인증 관련 컨트롤러
 * 로그인, 회원가입 등 사용자 인증 처리
 */
@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final UserService userService;

    /**
     * 로그인 페이지 표시
     *
     * @param error 로그인 오류 파라미터
     * @param model 뷰 모델
     * @return 로그인 페이지 뷰
     */
    @GetMapping("/login")
    public String loginPage(@RequestParam(value = "error", required = false) String error, Model model) {
        if (error != null) {
            model.addAttribute("error", "아이디 또는 비밀번호가 올바르지 않습니다.");
        }
        return "auth/login";
    }

    /**
     * 회원가입 페이지 표시
     *
     * @return 회원가입 페이지 뷰
     */
    @GetMapping("/signup")
    public String signupPage() {
        return "auth/signup";
    }

    /**
     * 회원가입 처리
     *
     * @param user 회원가입 정보
     * @return 처리 결과
     */
    @PostMapping("/signup")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> signup(@RequestBody User user) {
        Map<String, Object> response = new HashMap<>();

        try {
            log.info("회원가입 요청 - 사용자명: {}", user.getUsername());

            // 입력 값 검증
            if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "사용자명을 입력해주세요.");
                return ResponseEntity.badRequest().body(response);
            }

            if (user.getPassword() == null || user.getPassword().length() < 4) {
                response.put("success", false);
                response.put("message", "비밀번호는 최소 4자리 이상이어야 합니다.");
                return ResponseEntity.badRequest().body(response);
            }

            // 회원가입 처리
            User savedUser = userService.signup(user);

            response.put("success", true);
            response.put("message", "회원가입이 완료되었습니다.");
            response.put("username", savedUser.getUsername());

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            log.error("회원가입 실패: {}", e.getMessage());
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);

        } catch (Exception e) {
            log.error("회원가입 중 예상치 못한 오류 발생", e);
            response.put("success", false);
            response.put("message", "회원가입 중 오류가 발생했습니다. 다시 시도해주세요.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 사용자명 중복 확인
     *
     * @param username 확인할 사용자명
     * @return 중복 확인 결과
     */
    @GetMapping("/check-username")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> checkUsername(@RequestParam String username) {
        Map<String, Object> response = new HashMap<>();

        try {
            User existingUser = userService.findByUsername(username);
            boolean available = (existingUser == null);

            response.put("available", available);
            response.put("message", available ? "사용 가능한 사용자명입니다." : "이미 사용 중인 사용자명입니다.");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("사용자명 중복 확인 중 오류 발생", e);
            response.put("available", false);
            response.put("message", "확인 중 오류가 발생했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 로그아웃 처리
     *
     * @param request HTTP 요청
     * @return 메인 페이지로 리다이렉트
     */
    @PostMapping("/logout")
    public String logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        return "redirect:/";
    }

    /**
     * 현재 로그인된 사용자 정보 조회
     *
     * @param request HTTP 요청
     * @return 사용자 정보
     */
    @GetMapping("/user-info")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getUserInfo(HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();

        try {
            // Spring Security의 SecurityContext에서 사용자 정보를 가져오는 것이 더 적절하지만
            // 현재는 간단한 구현으로 세션을 사용
            HttpSession session = request.getSession(false);
            if (session != null) {
                String username = (String) session.getAttribute("username");
                if (username != null) {
                    User user = userService.findByUsername(username);
                    if (user != null) {
                        response.put("loggedIn", true);
                        response.put("username", user.getUsername());
                        response.put("email", user.getEmail());
                        response.put("role", user.getRole());
                        return ResponseEntity.ok(response);
                    }
                }
            }

            response.put("loggedIn", false);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("사용자 정보 조회 중 오류 발생", e);
            response.put("loggedIn", false);
            response.put("error", "사용자 정보를 가져오는 중 오류가 발생했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}