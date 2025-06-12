package kr.ac.kopo.lyh.personalcolor.controller;



import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import kr.ac.kopo.lyh.personalcolor.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;

@Controller
@RequiredArgsConstructor
@Slf4j
public class MainController {

    @GetMapping("/")
    public String home() {
        return "index";
    }

    @GetMapping("/upload")
    public String upload(HttpServletRequest request, Model model) {
        HttpSession session = request.getSession(false);

        // 세션이 없거나 사용자 정보가 없는 경우
        if (session == null || session.getAttribute("user") == null || session.getAttribute("isLoggedIn") == null) {
            return "redirect:/login";
        }

        // 로그인된 사용자 정보를 모델에 추가
        User user = (User) session.getAttribute("user");
        model.addAttribute("user", user);
        model.addAttribute("isLoggedIn", true);

        return "upload";
    }
    @GetMapping("/api/session")
    @ResponseBody
    public ResponseEntity<?> checkSession(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        boolean isLoggedIn = session != null && session.getAttribute("user") != null;

        if (isLoggedIn) {
            User user = (User) session.getAttribute("user");
            return ResponseEntity.ok(Map.of(
                    "isLoggedIn", true,
                    "userEmail", user.getEmail()
            ));
        } else {
            return ResponseEntity.ok(Map.of("isLoggedIn", false));
        }
    }


    @GetMapping("/menu")
    public String menu() {
        return "redirect:/shop";
    }

    @GetMapping("/shop")
    public String shop() {
        return "shop";
    }
}
