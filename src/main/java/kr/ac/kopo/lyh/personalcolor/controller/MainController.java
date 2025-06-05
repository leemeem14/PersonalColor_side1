package kr.ac.kopo.lyh.personalcolor.controller;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
@Slf4j
public class MainController {

    @GetMapping("/")
    public String home() {
        return "index";
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
