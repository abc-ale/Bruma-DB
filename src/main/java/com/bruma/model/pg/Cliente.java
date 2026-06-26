package com.bruma.model.pg;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "clientes")
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_cliente")
    private Integer idCliente;

    @Column(nullable = false, length = 150)
    private String nombre;

    @Column(unique = true, length = 20)
    private String dni;

    @Column(name = "fecha_registro")
    private LocalDate fechaRegistro;

    @OneToMany(mappedBy = "cliente", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Pedido> pedidos = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        if (fechaRegistro == null) fechaRegistro = LocalDate.now();
    }

    public Integer  getIdCliente()             { return idCliente; }
    public void     setIdCliente(Integer v)    { this.idCliente = v; }
    public String   getNombre()                { return nombre; }
    public void     setNombre(String v)        { this.nombre = v; }
    public String   getDni()                   { return dni; }
    public void     setDni(String v)           { this.dni = v; }
    public LocalDate getFechaRegistro()        { return fechaRegistro; }
    public void     setFechaRegistro(LocalDate v){ this.fechaRegistro = v; }
    public List<Pedido> getPedidos()           { return pedidos; }
    public void     setPedidos(List<Pedido> v) { this.pedidos = v; }
}
