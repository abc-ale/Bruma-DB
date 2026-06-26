package com.bruma.dto;

import java.util.List;

/**
 * DTO de solo lectura para la vista del cajero.
 * Solo expone el nombre del cliente y los nombres de los productos
 * de sus pedidos — sin datos personales (DNI, fechas, totales).
 */
public class ClienteResumenDTO {

    private Integer idCliente;
    private String  nombre;
    private List<String> productosDelPedido;  // e.g. ["Café latte x2", "Té verde x1"]

    public ClienteResumenDTO() {}

    public ClienteResumenDTO(Integer idCliente, String nombre, List<String> productosDelPedido) {
        this.idCliente          = idCliente;
        this.nombre             = nombre;
        this.productosDelPedido = productosDelPedido;
    }

    public Integer      getIdCliente()             { return idCliente; }
    public void         setIdCliente(Integer v)    { this.idCliente = v; }
    public String       getNombre()                { return nombre; }
    public void         setNombre(String v)        { this.nombre = v; }
    public List<String> getProductosDelPedido()    { return productosDelPedido; }
    public void         setProductosDelPedido(List<String> v){ this.productosDelPedido = v; }
}
