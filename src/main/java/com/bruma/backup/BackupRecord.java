package com.bruma.backup;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Registro de un respaldo generado.
 * Se mantiene en memoria durante la sesión; los archivos persisten en disco.
 */
public class BackupRecord {

    public enum Tipo { POSTGRESQL, MONGODB, COMPLETO }
    public enum Estado { OK, ERROR }

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    private final String        id;
    private final Tipo          tipo;
    private final Estado        estado;
    private final LocalDateTime fechaHora;
    private final String        rutaArchivo;
    private final long          tamanioBytes;
    private final String        mensajeError;

    public BackupRecord(String id, Tipo tipo, Estado estado,
                        LocalDateTime fechaHora, String rutaArchivo,
                        long tamanioBytes, String mensajeError) {
        this.id           = id;
        this.tipo         = tipo;
        this.estado       = estado;
        this.fechaHora    = fechaHora;
        this.rutaArchivo  = rutaArchivo;
        this.tamanioBytes = tamanioBytes;
        this.mensajeError = mensajeError;
    }

    // ── Getters ───────────────────────────────────────────────
    public String        getId()           { return id; }
    public Tipo          getTipo()         { return tipo; }
    public Estado        getEstado()       { return estado; }
    public LocalDateTime getFechaHora()    { return fechaHora; }
    public String        getRutaArchivo()  { return rutaArchivo; }
    public long          getTamanioBytes() { return tamanioBytes; }
    public String        getMensajeError() { return mensajeError; }

    public String getFechaHoraFormateada() {
        return fechaHora != null ? fechaHora.format(FMT) : "—";
    }

    public String getTamanioFormateado() {
        if (tamanioBytes <= 0) return "—";
        if (tamanioBytes < 1024)       return tamanioBytes + " B";
        if (tamanioBytes < 1024 * 1024) return String.format("%.1f KB", tamanioBytes / 1024.0);
        return String.format("%.2f MB", tamanioBytes / (1024.0 * 1024));
    }

    public String getNombreArchivo() {
        if (rutaArchivo == null) return "—";
        return rutaArchivo.contains("/")
            ? rutaArchivo.substring(rutaArchivo.lastIndexOf('/') + 1)
            : rutaArchivo;
    }
}
