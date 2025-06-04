package kr.ac.kopo.lyh.personalcolor.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ShopController {

    @GetMapping("/shop")
    public String shop() {
        return "shop";  // templates/shop.html을 반환
    }

    @GetMapping("/")
    public String home() {
        return "index";  // 메인 페이지 (index.html)
    }
}