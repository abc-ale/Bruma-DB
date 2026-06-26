package com.bruma.model.pg;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pedidos")
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pedido")
    private Integer idPedido;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_cliente", nullable = true)  // nullable: permite pedidos anónimos
    private Cliente cliente;

    @Column(name = "fecha_pedido")
    private LocalDateTime fechaPedido;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal total = BigDecimal.ZERO;

    // FIX 7: campo para guardar comentarios del pedido (ej: "Sin azúcar, Leche de almendras")
    @Column(name = "comentario", length = 500)
    private String comentario;

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<DetallePedido> detalles = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        if (fechaPedido == null) fechaPedido = LocalDateTime.now();
    }

    public Integer       getIdPedido()                { return idPedido; }
    public void          setIdPedido(Integer v)       { this.idPedido = v; }
    public Cliente       getCliente()                 { return cliente; }
    public void          setCliente(Cliente v)        { this.cliente = v; }
    public LocalDateTime getFechaPedido()             { return fechaPedido; }
    public void          setFechaPedido(LocalDateTime v){ this.fechaPedido = v; }
    public BigDecimal    getTotal()                   { return total; }
    public void          setTotal(BigDecimal v)       { this.total = v; }
    public String        getComentario()              { return comentario; }
    public void          setComentario(String v)      { this.comentario = v; }
    public List<DetallePedido> getDetalles()          { return detalles; }
    public void          setDetalles(List<DetallePedido> v){ this.detalles = v; }
}
