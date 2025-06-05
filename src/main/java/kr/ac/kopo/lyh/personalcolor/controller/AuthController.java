package kr.ac.kopo.lyh.personalcolor.controller;

import ch.qos.logback.core.model.Model;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final UserService userService;

    @GetMapping("/login")
    public String loginForm() {
        return "login";
    }

    @GetMapping("/signup")
    public String signupForm(Model model) {
        model.addAttribute("signupForm", new SignupForm());
        return "signup";
    }

    @PostMapping("/signup")
    public String signup(@ModelAttribute SignupForm signupForm,
                         BindingResult bindingResult,
                         Model model) {

        if (bindingResult.hasErrors()) {
            return "signup";
        }

        try {
            userService.createUser(signupForm);
            model.addAttribute("successMessage", "회원가입이 완료되었습니다.");
            return "login";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "signup";
        }
    }

    @PostMapping("/api/login")
    @ResponseBody
    public ResponseEntity<?> login(@RequestBody LoginRequest request,
                                   HttpServletRequest httpRequest) {
        try {
            User user = userService.authenticate(request.getEmail(), request.getPassword());

            // 세션에 사용자 정보 저장
            HttpSession session = httpRequest.getSession();
            session.setAttribute("user", user);
            session.setAttribute("isLoggedIn", true);

            return ResponseEntity.ok(LoginResponse.builder()
                    .success(true)
                    .message("로그인 성공")
                    .redirectUrl("/")
                    .build());

        } catch (Exception e) {
            return ResponseEntity.ok(LoginResponse.builder()
                    .success(false)
                    .message("이메일 또는 비밀번호가 올바르지 않습니다.")
                    .build());
        }
    }

    @PostMapping("/logout")
    @ResponseBody
    public ResponseEntity<?> logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        return ResponseEntity.ok().build();
    }
}
