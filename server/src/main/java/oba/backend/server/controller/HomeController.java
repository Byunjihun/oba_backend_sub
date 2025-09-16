package oba.backend.server.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home() {
        // 간단한 시작 페이지 (템플릿 없으면 /login으로 가도록 하는 용도)
        return "forward:/login";
    }

    @GetMapping("/login")
    public String login() {
        // 간단한 링크만 있는 페이지가 없다면 /oauth2/authorization/github 로 바로 리다이렉트
        return "forward:/oauth2/authorization/github";
    }
}
