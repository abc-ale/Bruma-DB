package com.bruma.controller;

import com.bruma.repository.ProductoRepository;
import com.bruma.service.PedidoService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.Arrays;
import java.util.List;

@Controller
@RequestMapping("/pedidos")
public class PedidoController {

    private final PedidoService      service;
    private final ProductoRepository productoRepo;

    // PUNTO 4: 13 comentarios ordenados — leches juntas al inicio, luego el resto
    private static final List<String> COMENTARIOS_DISPONIBLES = Arrays.asList(
        "Leche de almendras",
        "Leche descremada",
        "Leche de coco",
        "Extra shot de espresso",
        "Temperatura extra caliente",
        "Alergia a lácteos",
        "Con sirope de vainilla",
        "Con chispas de chocolate",
        "Sin azúcar",
        "Todo para llevar",
        "Poco hielo",
        "Extra hielo",
        "Sin cafeína"
    );

    public PedidoController(PedidoService service, ProductoRepository productoRepo) {
        this.service      = service;
        this.productoRepo = productoRepo;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("pedidos", service.listar());
        return "pedidos";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("productos",              productoRepo.findAll());
        model.addAttribute("comentariosDisponibles", COMENTARIOS_DISPONIBLES);
        model.addAttribute("metodosPago",
            List.of("efectivo", "yape", "plin", "tarjeta", "transferencia"));
        return "pedidoForm";
    }

    @PostMapping("/guardar")
    public String guardar(
            @RequestParam String dni,
            @RequestParam String nombreCliente,
            @RequestParam("idProducto")  List<Integer> idsProducto,
            @RequestParam("cantidad")    List<Integer> cantidades,
            @RequestParam(value = "metodoPago",  required = false, defaultValue = "efectivo") String metodoPago,
            // FIX 7: recibe la lista de comentarios seleccionados
            @RequestParam(value = "comentarios", required = false) List<String> comentarios) {

        service.crearPedido(dni, nombreCliente, idsProducto, cantidades, metodoPago, comentarios);
        return "redirect:/pedidos";
    }

    // FIX 3: /ver/{id} usa findById — detalles se cargan con EAGER en DetallePedido
    @GetMapping("/ver/{id}")
    public String ver(@PathVariable Integer id, Model model) {
        service.buscarPorId(id).ifPresentOrElse(
            p -> model.addAttribute("pedido", p),
            () -> model.addAttribute("errorMsg", "Pedido #" + id + " no encontrado.")
        );
        return "pedidoDetalle";
    }
}
