package com.bolao2027.modules.shared.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controller para rotas compartilhadas: raiz, login, registro.
 */
@Controller
public class HomeController {

    @GetMapping("/")
    public String home() {
        return "redirect:/matches";
    }

    @GetMapping("/login")
    public String login() {
        return "auth/login";
    }
}
