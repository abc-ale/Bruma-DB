package com.bruma.controller;

import com.bruma.dto.ClienteResumenDTO;
import com.bruma.model.pg.Cliente;
import com.bruma.model.pg.DetallePedido;
import com.bruma.model.pg.Pedido;
import com.bruma.service.ClienteService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/clientes")
public class ClienteController {

    private final ClienteService service;

    public ClienteController(ClienteService service) { this.service = service; }

    // FIX 4: /clientes accesible a ADMIN y CAJERO
    @PreAuthorize("hasAnyRole('ADMIN','CAJERO')")
    @GetMapping
    public String listar(Model model) {
        model.addAttribute("clientes", service.listar());
        // clienteForm solo lo necesita ADMIN en la misma página
        model.addAttribute("clienteForm", new Cliente());
        return "clientes";
    }

    // Vista CAJERO: solo nombre + productos de sus pedidos
    @PreAuthorize("hasAnyRole('ADMIN','CAJERO')")
    @GetMapping("/resumen")
    public String resumenCajero(Model model) {
        List<Cliente> clientes = service.listar();
        List<ClienteResumenDTO> resumen = new ArrayList<>();
        for (Cliente c : clientes) {
            List<String> items = new ArrayList<>();
            if (c.getPedidos() != null) {
                for (Pedido p : c.getPedidos()) {
                    if (p.getDetalles() != null) {
                        for (DetallePedido d : p.getDetalles()) {
                            if (d.getProducto() != null)
                                items.add(d.getProducto().getNombre() + " x" + d.getCantidad());
                        }
                    }
                }
            }
            resumen.add(new ClienteResumenDTO(c.getIdCliente(), c.getNombre(), items));
        }
        model.addAttribute("resumen", resumen);
        return "clientesResumen";
    }

    @PreAuthorize("hasAnyRole('ADMIN','CAJERO')")
    @GetMapping("/ver/{id}")
    public String ver(@PathVariable Integer id, Model model) {
        service.buscarPorId(id).ifPresent(c -> {
            model.addAttribute("cliente", c);
            model.addAttribute("info",    service.getInfo(id).orElse(null));
        });
        return "clientePerfil";
    }

    // FIX 2: editar — el form usa th:object=${clienteForm}, debe existir en el modelo
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Integer id, Model model) {
        service.buscarPorId(id).ifPresentOrElse(
            c -> model.addAttribute("clienteForm", c),
            () -> model.addAttribute("clienteForm", new Cliente())
        );
        return "clienteForm";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/guardar")
    public String guardar(@ModelAttribute("clienteForm") Cliente c,
                          RedirectAttributes flash) {
        service.guardar(c);
        flash.addFlashAttribute("ok", "Cliente guardado correctamente.");
        return "redirect:/clientes";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Integer id, RedirectAttributes flash) {
        service.eliminar(id);
        flash.addFlashAttribute("ok", "Cliente eliminado.");
        return "redirect:/clientes";
    }

    @PostMapping("/comentario")
    public String comentario(@RequestParam Integer id, @RequestParam String texto) {
        service.agregarComentario(id, texto);
        return "redirect:/clientes/ver/" + id;
    }

    @PostMapping("/preferencias")
    public String preferencias(@RequestParam Integer id,
                               @RequestParam String metodoPago) {
        service.guardarPreferencias(id, metodoPago);
        return "redirect:/clientes/ver/" + id;
    }
}
