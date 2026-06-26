package com.bruma.config;

import com.bruma.model.pg.Usuario;
import com.bruma.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Crea los usuarios por defecto al iniciar la app si no existen.
 *
 *  admin   / admin123   → ROLE_ADMIN
 *  cajero  / cajero123  → ROLE_CAJERO
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private final UsuarioRepository repo;
    private final PasswordEncoder   encoder;

    public DataInitializer(UsuarioRepository repo, PasswordEncoder encoder) {
        this.repo    = repo;
        this.encoder = encoder;
    }

    @Override
    public void run(String... args) {
        if (!repo.existsByUsername("admin")) {
            repo.save(new Usuario("admin",  encoder.encode("admin123"),  "ROLE_ADMIN",  "Administrador"));
            repo.save(new Usuario("cajero", encoder.encode("cajero123"), "ROLE_CAJERO", "Cajero Bruma"));
            System.out.println("✓ Usuarios creados: admin / cajero");
        }
    }
}
