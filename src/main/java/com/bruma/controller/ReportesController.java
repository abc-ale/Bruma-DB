package com.bruma.controller;

import com.bruma.model.mongo.ClienteInfo;
import com.bruma.model.pg.Cliente;
import com.bruma.repository.ClienteInfoRepository;
import com.bruma.repository.ClienteRepository;
import com.bruma.repository.PedidoRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import java.math.BigDecimal;
import java.util.*;

@Controller
@RequestMapping("/reportes")
public class ReportesController {

    private final ClienteRepository     clienteRepo;
    private final ClienteInfoRepository infoRepo;
    private final PedidoRepository      pedidoRepo;

    public ReportesController(ClienteRepository clienteRepo,
                              ClienteInfoRepository infoRepo,
                              PedidoRepository pedidoRepo) {
        this.clienteRepo = clienteRepo;
        this.infoRepo    = infoRepo;
        this.pedidoRepo  = pedidoRepo;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public String reportes(Model model) {

        List<Cliente>     clientes = clienteRepo.findAll();
        List<ClienteInfo> infos    = infoRepo.findAll();
        // FIX 1: usar findAllWithCliente() y proteger contra cliente null
        List<com.bruma.model.pg.Pedido> pedidos = pedidoRepo.findAllWithCliente();

        // Índice MongoDB por id_cliente
        Map<Integer, ClienteInfo> infoMap = new HashMap<>();
        for (ClienteInfo ci : infos) infoMap.put(ci.getIdCliente(), ci);

        // FIX 1: proteger contra p.getCliente() == null (pedidos anónimos)
        Map<Integer, BigDecimal> totalPorCliente   = new HashMap<>();
        Map<Integer, Integer>    pedidosPorCliente = new HashMap<>();
        for (com.bruma.model.pg.Pedido p : pedidos) {
            if (p.getCliente() == null) continue; // skip pedidos anónimos
            int id = p.getCliente().getIdCliente();
            totalPorCliente.merge(id, p.getTotal(), BigDecimal::add);
            pedidosPorCliente.merge(id, 1, Integer::sum);
        }

        // Vista unificada
        List<Map<String, Object>> perfiles = new ArrayList<>();
        for (Cliente c : clientes) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("idCliente",  c.getIdCliente());
            row.put("nombre",     c.getNombre());
            row.put("dni",        c.getDni());
            ClienteInfo info = infoMap.get(c.getIdCliente());
            if (info != null) {
                row.put("nComentarios",     info.getComentarios().size());
                row.put("metodoPago",       info.getPreferencias().getMetodoPago());
                row.put("notif",            info.getPreferencias().getNotificaciones());
                row.put("tienePerfilMongo", true);
            } else {
                row.put("nComentarios", 0);
                row.put("metodoPago",   "—");
                row.put("notif",        false);
                row.put("tienePerfilMongo", false);
            }
            row.put("totalCompras", totalPorCliente.getOrDefault(c.getIdCliente(), BigDecimal.ZERO));
            row.put("numPedidos",   pedidosPorCliente.getOrDefault(c.getIdCliente(), 0));
            perfiles.add(row);
        }

        // Reporte 2: Métodos de pago
        Map<String, Long> metodosPago = new LinkedHashMap<>();
        for (ClienteInfo ci : infos) {
            String m = ci.getPreferencias().getMetodoPago();
            metodosPago.merge(m, 1L, Long::sum);
        }

        BigDecimal totalVentas = pedidos.stream()
            .map(com.bruma.model.pg.Pedido::getTotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        long pedidosConCliente  = pedidos.stream().filter(p -> p.getCliente() != null).count();
        long pedidosAnonimos    = pedidos.size() - pedidosConCliente;

        model.addAttribute("perfiles",           perfiles);
        model.addAttribute("metodosPago",        metodosPago.entrySet());
        model.addAttribute("totalClientes",      clientes.size());
        model.addAttribute("totalPerfilesMongo", infos.size());
        model.addAttribute("totalPedidos",       pedidos.size());
        model.addAttribute("pedidosAnonimos",    pedidosAnonimos);
        model.addAttribute("totalVentas",        totalVentas);

        return "reportes";
    }
}
