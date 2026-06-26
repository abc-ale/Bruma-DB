package com.bruma.controller;

import com.bruma.repository.ClienteRepository;
import com.bruma.repository.ProductoRepository;
import com.bruma.service.PedidoService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    private final ClienteRepository  clienteRepo;
    private final ProductoRepository productoRepo;
    private final PedidoService      pedidoService;

    public DashboardController(ClienteRepository clienteRepo,
                               ProductoRepository productoRepo,
                               PedidoService pedidoService) {
        this.clienteRepo  = clienteRepo;
        this.productoRepo = productoRepo;
        this.pedidoService = pedidoService;
    }

    @GetMapping("/")
    public String dashboard(Model model) {
        boolean pg = true;
        try {
            model.addAttribute("totalClientes",    clienteRepo.count());
            model.addAttribute("totalProductos",   productoRepo.count());
            model.addAttribute("totalPedidos",     pedidoService.listar().size());
            model.addAttribute("pedidosRecientes", pedidoService.ultimos5());
        } catch (Exception e) {
            pg = false;
            model.addAttribute("totalClientes",    0);
            model.addAttribute("totalProductos",   0);
            model.addAttribute("totalPedidos",     0);
            model.addAttribute("pedidosRecientes", java.util.Collections.emptyList());
        }
        model.addAttribute("pgConectado", pg);
        return "dashboard";
    }
}
