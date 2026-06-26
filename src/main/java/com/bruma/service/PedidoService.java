package com.bruma.service;

import com.bruma.model.mongo.ClienteInfo;
import com.bruma.model.pg.*;
import com.bruma.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class PedidoService {

    private final PedidoRepository      pedidoRepo;
    private final ProductoRepository    productoRepo;
    private final ClienteService        clienteService;
    private final ClienteInfoRepository infoRepo;

    public PedidoService(PedidoRepository pedidoRepo,
                         ProductoRepository productoRepo,
                         ClienteService clienteService,
                         ClienteInfoRepository infoRepo) {
        this.pedidoRepo     = pedidoRepo;
        this.productoRepo   = productoRepo;
        this.clienteService = clienteService;
        this.infoRepo       = infoRepo;
    }

    public List<Pedido>     listar()               { return pedidoRepo.findAllWithCliente(); }
    public List<Pedido>     ultimos5()             { return pedidoRepo.findTop5(); }
    public Optional<Pedido> buscarPorId(Integer id){ return pedidoRepo.findByIdWithDetalles(id); } // FIX #6

    @Transactional
    public Pedido crearPedido(String dni,
                              String nombreCliente,
                              List<Integer> idsProducto,
                              List<Integer> cantidades,
                              String metodoPago,
                              List<String> comentariosSeleccionados) {

        // 1. Buscar o crear cliente por DNI
        Cliente cliente = clienteService.buscarOCrearPorDni(dni, nombreCliente);

        // 2. Construir pedido
        Pedido pedido = new Pedido();
        pedido.setCliente(cliente);

        // FIX 7: unir comentarios seleccionados y guardarlos en el campo comentario del pedido
        if (comentariosSeleccionados != null && !comentariosSeleccionados.isEmpty()) {
            String textoComentario = String.join(", ", comentariosSeleccionados
                .stream().filter(c -> c != null && !c.isBlank()).toList());
            pedido.setComentario(textoComentario);
        }

        // 3. Construir detalles
        BigDecimal total = BigDecimal.ZERO;
        for (int i = 0; i < idsProducto.size(); i++) {
            Integer idProd = idsProducto.get(i);
            Integer cant   = cantidades.get(i);
            if (idProd == null || cant == null || cant <= 0) continue;

            Producto prod = productoRepo.findById(idProd)
                .orElseThrow(() -> new IllegalArgumentException("Producto no existe: " + idProd));

            DetallePedido det = new DetallePedido();
            det.setPedido(pedido);
            det.setProducto(prod);
            det.setCantidad(cant);
            BigDecimal sub = prod.getPrecio().multiply(BigDecimal.valueOf(cant));
            det.setSubtotal(sub);
            total = total.add(sub);
            pedido.getDetalles().add(det);
        }
        pedido.setTotal(total);
        Pedido saved = pedidoRepo.save(pedido);

        // 4. Persistir en MongoDB: método de pago y comentarios como notas del cliente
        Integer idCliente = cliente.getIdCliente();
        ClienteInfo info = infoRepo.findByIdCliente(idCliente).orElseGet(() -> {
            ClienteInfo ni = new ClienteInfo();
            ni.setIdCliente(idCliente);
            return ni;
        });

        if (metodoPago != null && !metodoPago.isBlank()) {
            info.getPreferencias().setMetodoPago(metodoPago);
        }

        // Guardar comentarios como notas en MongoDB también (historial del cliente)
        if (comentariosSeleccionados != null && !comentariosSeleccionados.isEmpty()) {
            String hoy = LocalDate.now().toString();
            String resumen = "Pedido #" + saved.getIdPedido() + ": " +
                String.join(", ", comentariosSeleccionados);
            ClienteInfo.Comentario c = new ClienteInfo.Comentario();
            c.setTexto(resumen);
            c.setFecha(hoy);
            info.getComentarios().add(c);
        }
        infoRepo.save(info);
        return saved;
    }
}
