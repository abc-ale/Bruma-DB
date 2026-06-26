package com.bruma.model.pg;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "productos")
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_producto")
    private Integer idProducto;

    @Column(nullable = false, length = 150)
    private String nombre;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal precio;

    @Column(length = 50)
    private String categoria;

    @Column(length = 300)
    private String descripcion;

    public Integer    getIdProducto()            { return idProducto; }
    public void       setIdProducto(Integer v)   { this.idProducto = v; }
    public String     getNombre()                { return nombre; }
    public void       setNombre(String v)        { this.nombre = v; }
    public BigDecimal getPrecio()                { return precio; }
    public void       setPrecio(BigDecimal v)    { this.precio = v; }
    public String     getCategoria()             { return categoria; }
    public void       setCategoria(String v)     { this.categoria = v; }
    public String     getDescripcion()           { return descripcion; }
    public void       setDescripcion(String v)   { this.descripcion = v; }
}
