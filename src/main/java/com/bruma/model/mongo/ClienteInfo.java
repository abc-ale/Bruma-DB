package com.bruma.model.mongo;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "clientes_info")
public class ClienteInfo {

    @Id
    private String id;

    @Field("id_cliente")
    private Integer idCliente;

    private List<Comentario> comentarios = new ArrayList<>();
    private Preferencias preferencias = new Preferencias();

    // ── Nested: Comentario ──────────────────────────────────────
    public static class Comentario {
        private String texto;
        private String fecha;
        public String getTexto()        { return texto; }
        public void   setTexto(String v){ this.texto = v; }
        public String getFecha()        { return fecha; }
        public void   setFecha(String v){ this.fecha = v; }
    }

    // ── Nested: Preferencias ────────────────────────────────────
    public static class Preferencias {
        private String  idioma      = "es";
        private String  metodoPago  = "efectivo";
        private boolean notificaciones = true;
        public String  getIdioma()               { return idioma; }
        public void    setIdioma(String v)        { this.idioma = v; }
        public String  getMetodoPago()            { return metodoPago; }
        public void    setMetodoPago(String v)    { this.metodoPago = v; }
        public boolean isNotificaciones()          { return notificaciones; }
        public boolean getNotificaciones()         { return notificaciones; }
        public void    setNotificaciones(boolean v){ this.notificaciones = v; }
    }

    public String          getId()                       { return id; }
    public void            setId(String v)               { this.id = v; }
    public Integer         getIdCliente()                { return idCliente; }
    public void            setIdCliente(Integer v)       { this.idCliente = v; }
    public List<Comentario>getComentarios()              { return comentarios; }
    public void            setComentarios(List<Comentario> v){ this.comentarios = v; }
    public Preferencias    getPreferencias()             { return preferencias; }
    public void            setPreferencias(Preferencias v){ this.preferencias = v; }
}
