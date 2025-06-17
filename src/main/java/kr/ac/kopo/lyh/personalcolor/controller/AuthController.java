package kr.ac.kopo.lyh.personalcolor.controller;

import kr.ac.kopo.lyh.personalcolor.controller.dto.LoginRequest;
import kr.ac.kopo.lyh.personalcolor.controller.dto.LoginResponse;
import kr.ac.kopo.lyh.personalcolor.controller.dto.SignupRequest;
import kr.ac.kopo.lyh.personalcolor.entity.User;
import kr.ac.kopo.lyh.personalcolor.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final UserService userService;

    @GetMapping("/login")
    public String loginForm(HttpServletRequest request, Model model) {
        // 이미 로그인된 사용자는 메인 페이지로 리다이렉트
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !auth.getPrincipal().equals("anonymousUser")) {
            return "redirect:/";
        }

        // CSRF 토큰을 모델에 추가
        model.addAttribute("_csrf", request.getAttribute("_csrf"));
        return "login";
    }

    @GetMapping("/signup")
    public String signupForm() {
        return "signup";
    }

    @PostMapping("/api/login")
    @ResponseBody
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request,
                                               HttpServletRequest httpRequest) {
        try {
            // 사용자 인증
            User user = userService.authenticate(request.getEmail(), request.getPassword());

            // Spring Security 인증 토큰 생성
            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(user.getEmail(), null, Collections.emptyList());
            SecurityContextHolder.getContext().setAuthentication(authToken);

            // 세션에 Spring Security 컨텍스트 저장
            HttpSession session = httpRequest.getSession();
            session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                    SecurityContextHolder.getContext());

            // 추가 사용자 정보 세션에 저장
            session.setAttribute("user", user);
            session.setAttribute("isLoggedIn", true);
            session.setAttribute("userEmail", user.getEmail());

            log.info("사용자 로그인 성공: {}", user.getEmail());

            return ResponseEntity.ok(LoginResponse.builder()
                    .success(true)
                    .message("로그인 성공")
                    .redirectUrl("/upload") // 로그인 후 바로 업로드 페이지로 이동
                    .userEmail(user.getEmail())
                    .build());

        } catch (Exception e) {
            log.error("로그인 실패", e);
            return ResponseEntity.ok(LoginResponse.builder()
                    .success(false)
                    .message("이메일 또는 비밀번호가 올바르지 않습니다.")
                    .build());
        }
    }

    @PostMapping("/api/signup")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> signup(@RequestBody SignupRequest request) {
        Map<String, Object> response = new HashMap<>();

        try {
            // 이메일 중복 체크
            if (userService.existsByEmail(request.getEmail())) {
                response.put("success", false);
                response.put("message", "이미 존재하는 이메일입니다.");
                return ResponseEntity.ok(response);
            }

            // 회원가입 처리
            User newUser = userService.createUser(request.getEmail(), request.getPassword(), request.getName());

            response.put("success", true);
            response.put("message", "회원가입이 완료되었습니다.");
            response.put("redirectUrl", "/login");

            log.info("새 사용자 회원가입: {}", newUser.getEmail());

        } catch (Exception e) {
            log.error("회원가입 실패", e);
            response.put("success", false);
            response.put("message", "회원가입 중 오류가 발생했습니다.");
        }

        return ResponseEntity.ok(response);
    }

    /**
     * API를 통한 로그아웃 처리 (AJAX 요청용)
     */
    @PostMapping("/api/logout")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> logoutApi(HttpServletRequest request,
                                                         HttpServletResponse response) throws IOException {
        Map<String, Object> result = new HashMap<>();

        try {
            // 세션 무효화
            HttpSession session = request.getSession(false);
            if (session != null) {
                session.invalidate();
            }

            // Security Context 클리어
            SecurityContextHolder.clearContext();

            log.info("API 로그아웃 성공");

            result.put("success", true);
            result.put("message", "로그아웃되었습니다.");
            result.put("redirectUrl", "/");

        } catch (Exception e) {
            log.error("API 로그아웃 실패", e);
            result.put("success", false);
            result.put("message", "로그아웃 중 오류가 발생했습니다.");
        }

        return ResponseEntity.ok(result);
    }

    /**
     * 현재 로그인된 사용자 정보 조회
     */
    @GetMapping("/api/user/current")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getCurrentUser(HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();

        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            HttpSession session = request.getSession(false);

            if (auth != null && auth.isAuthenticated() && !auth.getPrincipal().equals("anonymousUser")) {
                User user = (User) session.getAttribute("user");

                response.put("success", true);
                response.put("isAuthenticated", true);
                response.put("userEmail", user != null ? user.getEmail() : auth.getName());

            } else {
                response.put("success", true);
                response.put("isAuthenticated", false);
            }

        } catch (Exception e) {
            log.error("사용자 정보 조회 실패", e);
            response.put("success", false);
            response.put("isAuthenticated", false);
            response.put("message", "사용자 정보 조회 중 오류가 발생했습니다.");
        }

        return ResponseEntity.ok(response);
    }
}
