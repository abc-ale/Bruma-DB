package com.bruma.model.pg;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "detalle_pedido")
public class DetallePedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_detalle")
    private Integer idDetalle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_pedido", nullable = false)
    private Pedido pedido;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_producto", nullable = false)
    private Producto producto;

    @Column(nullable = false)
    private Integer cantidad;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;

    public Integer       getIdDetalle()           { return idDetalle; }
    public void          setIdDetalle(Integer v)  { this.idDetalle = v; }
    public Pedido        getPedido()              { return pedido; }
    public void          setPedido(Pedido v)      { this.pedido = v; }
    public Producto      getProducto()            { return producto; }
    public void          setProducto(Producto v)  { this.producto = v; }
    public Integer       getCantidad()            { return cantidad; }
    public void          setCantidad(Integer v)   { this.cantidad = v; }
    public BigDecimal    getSubtotal()            { return subtotal; }
    public void          setSubtotal(BigDecimal v){ this.subtotal = v; }
}
