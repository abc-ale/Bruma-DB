package com.bruma.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration cfg) throws Exception {
        return cfg.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                // ── Públicos ──────────────────────────────────────────────
                .requestMatchers("/css/**", "/js/**", "/img/**", "/login", "/error").permitAll()
                // Página de acceso denegado — visible para cualquier usuario autenticado
                .requestMatchers("/acceso-denegado").authenticated()

                // ── SOLO ADMIN ─────────────────────────────────────────────
                // PUNTO 1: Clientes exclusivo de ADMIN — CAJERO y MESERO no tienen acceso
                .requestMatchers("/clientes", "/clientes/**").hasRole("ADMIN")
                // Gestión de productos (escritura)
                .requestMatchers("/productos/eliminar/**", "/productos/editar/**",
                                 "/productos/guardar").hasRole("ADMIN")
                // Respaldos periódicos
                .requestMatchers("/respaldos", "/respaldos/**").hasRole("ADMIN")
                // Reportes
                .requestMatchers("/reportes", "/reportes/**").hasRole("ADMIN")
                // Registro de usuarios
                .requestMatchers("/usuarios/nuevo", "/usuarios/guardar").hasRole("ADMIN")
                .requestMatchers("/registrar", "/registrar/guardar").hasRole("ADMIN")

                // ── CAJERO + MESERO + ADMIN ────────────────────────────────
                .requestMatchers("/productos").hasAnyRole("ADMIN", "CAJERO", "MESERO")
                .requestMatchers("/pedidos", "/pedidos/**").hasAnyRole("ADMIN", "CAJERO", "MESERO")

                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/", true)
                .failureUrl("/login?error=true")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout=true")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .addLogoutHandler(new SecurityContextLogoutHandler())
                .permitAll()
            )
            // Redirige a una página personalizada "Ups, ruta incorrecta"
            // en lugar de la pantalla blanca genérica de Spring
            .exceptionHandling(ex -> ex
                .accessDeniedHandler((request, response, accessDeniedException) ->
                    response.sendRedirect(request.getContextPath() + "/acceso-denegado")
                )
            );
        return http.build();
    }
}
