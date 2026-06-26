package com.bruma.model.pg;

import jakarta.persistence.*;

@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true, length = 80)
    private String username;

    @Column(nullable = false)
    private String password;

    /** ROLE_ADMIN  o  ROLE_CAJERO */
    @Column(nullable = false, length = 30)
    private String rol;

    @Column(nullable = false, length = 120)
    private String nombre;

    public Usuario() {}
    public Usuario(String username, String password, String rol, String nombre) {
        this.username = username; this.password = password;
        this.rol = rol; this.nombre = nombre;
    }

    public Integer getId()              { return id; }
    public void    setId(Integer v)     { this.id = v; }
    public String  getUsername()        { return username; }
    public void    setUsername(String v){ this.username = v; }
    public String  getPassword()        { return password; }
    public void    setPassword(String v){ this.password = v; }
    public String  getRol()             { return rol; }
    public void    setRol(String v)     { this.rol = v; }
    public String  getNombre()          { return nombre; }
    public void    setNombre(String v)  { this.nombre = v; }
}
