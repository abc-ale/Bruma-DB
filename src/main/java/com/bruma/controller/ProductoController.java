package com.bruma.controller;

import com.bruma.model.pg.Producto;
import com.bruma.repository.ProductoRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/productos")
public class ProductoController {

    private final ProductoRepository repo;

    public ProductoController(ProductoRepository repo) { this.repo = repo; }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("productos",    repo.findAll());
        model.addAttribute("productoForm", new Producto());
        return "productos";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Integer id, Model model) {
        repo.findById(id).ifPresent(p -> model.addAttribute("productoForm", p));
        return "productoForm";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/guardar")
    public String guardar(@ModelAttribute("productoForm") Producto p) {
        repo.save(p);
        return "redirect:/productos";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Integer id) {
        repo.deleteById(id);
        return "redirect:/productos";
    }
}
