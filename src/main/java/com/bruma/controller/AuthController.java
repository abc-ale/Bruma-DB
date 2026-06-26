package com.bruma.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AuthController {

    // Fix 4: mapea /login para que Thymeleaf encuentre login.html
    // Sin este método Spring busca un recurso estático y lanza NoResourceFoundException
    @GetMapping("/login")
    public String login() {
        return "login";
    }

    // Página personalizada de acceso denegado — reemplaza el Whitelabel Error Page
    @GetMapping("/acceso-denegado")
    public String accesoDenegado() {
        return "accesoDenegado";
    }
}
