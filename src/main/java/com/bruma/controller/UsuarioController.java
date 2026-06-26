package com.bruma.controller;

import com.bruma.model.pg.Usuario;
import com.bruma.repository.UsuarioRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class UsuarioController {

    private final UsuarioRepository repo;
    private final PasswordEncoder   encoder;

    public UsuarioController(UsuarioRepository repo, PasswordEncoder encoder) {
        this.repo    = repo;
        this.encoder = encoder;
    }

    // Ruta original (se mantiene sin cambios)
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/usuarios/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("usuarioForm", new Usuario());
        model.addAttribute("roles", java.util.List.of("ROLE_ADMIN", "ROLE_CAJERO", "ROLE_MESERO"));
        return "usuarioForm";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/usuarios/guardar")
    public String guardar(@RequestParam String username,
                          @RequestParam String password,
                          @RequestParam String rol,
                          @RequestParam String nombre,
                          RedirectAttributes flash) {

        if (repo.existsByUsername(username)) {
            flash.addFlashAttribute("error", "El usuario '" + username + "' ya existe.");
            return "redirect:/usuarios/nuevo";
        }
        if (password == null || password.length() < 6) {
            flash.addFlashAttribute("error", "La contraseña debe tener al menos 6 caracteres.");
            return "redirect:/usuarios/nuevo";
        }
        Usuario u = new Usuario();
        u.setUsername(username.trim().toLowerCase());
        u.setPassword(encoder.encode(password));
        u.setRol(rol);
        u.setNombre(nombre.trim());
        repo.save(u);
        flash.addFlashAttribute("ok", "Usuario '" + username + "' creado con rol " + rol + ".");
        return "redirect:/usuarios/nuevo";
    }

    // FIX #4: Nueva ruta /registrar — exclusiva para ROLE_ADMIN
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/registrar")
    public String mostrarRegistrar(Model model) {
        model.addAttribute("roles", java.util.List.of("ROLE_ADMIN", "ROLE_CAJERO", "ROLE_MESERO"));
        return "registrar";
    }

    // FIX #4: Recibe la contraseña en texto plano y la encripta con BCrypt antes de persistir
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/registrar/guardar")
    public String guardarRegistrar(@RequestParam String username,
                                   @RequestParam String password,
                                   @RequestParam String rol,
                                   @RequestParam String nombre,
                                   RedirectAttributes flash) {
        if (repo.existsByUsername(username)) {
            flash.addFlashAttribute("error", "El usuario '" + username + "' ya existe.");
            return "redirect:/registrar";
        }
        if (password == null || password.length() < 6) {
            flash.addFlashAttribute("error", "La contraseña debe tener al menos 6 caracteres.");
            return "redirect:/registrar";
        }
        Usuario u = new Usuario();
        u.setUsername(username.trim().toLowerCase());
        u.setPassword(encoder.encode(password)); // BCrypt automático
        u.setRol(rol);
        u.setNombre(nombre.trim());
        repo.save(u);
        flash.addFlashAttribute("ok", "Usuario '" + username + "' creado con rol " + rol + ".");
        return "redirect:/registrar";
    }
}
