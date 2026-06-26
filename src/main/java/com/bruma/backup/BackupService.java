package com.bruma.backup;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.PostConstruct;
import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class BackupService {

    private static final Logger log = LoggerFactory.getLogger(BackupService.class);
    private static final DateTimeFormatter TS = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
    private static final int MAX_RESPALDOS = 30;

    private final com.bruma.repository.ClienteRepository    clienteRepo;
    private final com.bruma.repository.PedidoRepository     pedidoRepo;
    private final com.bruma.repository.ProductoRepository   productoRepo;
    private final com.bruma.repository.UsuarioRepository    usuarioRepo;
    private final com.bruma.repository.ClienteInfoRepository infoRepo;

    private final List<BackupRecord> historial = new CopyOnWriteArrayList<>();

    @Value("${bruma.backup.directorio:backups}")
    private String directorioBase;

    public BackupService(com.bruma.repository.ClienteRepository clienteRepo,
                         com.bruma.repository.PedidoRepository pedidoRepo,
                         com.bruma.repository.ProductoRepository productoRepo,
                         com.bruma.repository.UsuarioRepository usuarioRepo,
                         com.bruma.repository.ClienteInfoRepository infoRepo) {
        this.clienteRepo  = clienteRepo;
        this.pedidoRepo   = pedidoRepo;
        this.productoRepo = productoRepo;
        this.usuarioRepo  = usuarioRepo;
        this.infoRepo     = infoRepo;
    }

    @PostConstruct
    public void inicializar() throws IOException {
        Path dir = Paths.get(directorioBase);
        if (!Files.exists(dir)) {
            Files.createDirectories(dir);
            log.info("Directorio de respaldos creado: {}", dir.toAbsolutePath());
        }
        cargarHistorialDesconocido(dir);
    }

    // ── Scheduler ─────────────────────────────────────────────

    @Scheduled(cron = "0 0 2 * * *")
    public void respaldoDiario() {
        log.info("Respaldo diario automático iniciado");
        ejecutarRespaldoCompleto("AUTO-DIARIO");
    }

    @Scheduled(cron = "0 0 3 * * MON")
    public void respaldoSemanal() {
        log.info("Respaldo semanal automático iniciado");
        ejecutarRespaldoCompleto("AUTO-SEMANAL");
    }

    // ── API pública ────────────────────────────────────────────

    public BackupRecord ejecutarRespaldoPostgres() {
        return respaldarPostgres("MANUAL");
    }

    public BackupRecord ejecutarRespaldoMongo() {
        return respaldarMongo("MANUAL");
    }

    public BackupRecord ejecutarRespaldoCompleto(String origen) {
        BackupRecord pg    = respaldarPostgres(origen);
        BackupRecord mongo = respaldarMongo(origen);

        boolean ok = pg.getEstado()    == BackupRecord.Estado.OK
                  && mongo.getEstado() == BackupRecord.Estado.OK;

        String pgErr    = pg.getMensajeError()    != null ? pg.getMensajeError()    : "ok";
        String mongoErr = mongo.getMensajeError() != null ? mongo.getMensajeError() : "ok";

        String id = "FULL-" + LocalDateTime.now().format(TS);
        BackupRecord full = new BackupRecord(
            id,
            BackupRecord.Tipo.COMPLETO,
            ok ? BackupRecord.Estado.OK : BackupRecord.Estado.ERROR,
            LocalDateTime.now(),
            (pg.getRutaArchivo() != null ? pg.getRutaArchivo() : "") + " | " +
            (mongo.getRutaArchivo() != null ? mongo.getRutaArchivo() : ""),
            pg.getTamanioBytes() + mongo.getTamanioBytes(),
            ok ? null : "PG=" + pgErr + " | Mongo=" + mongoErr
        );
        agregarHistorial(full);
        limpiarRespaldosAntiguos();
        return full;
    }

    public List<BackupRecord> obtenerHistorial() {
        List<BackupRecord> copia = new ArrayList<>(historial);
        copia.sort(Comparator.comparing(BackupRecord::getFechaHora).reversed());
        return copia;
    }

    public Optional<Path> obtenerRuta(String id) {
        return historial.stream()
            .filter(r -> r.getId().equals(id))
            .filter(r -> r.getRutaArchivo() != null && !r.getRutaArchivo().isBlank())
            .map(r -> Paths.get(r.getRutaArchivo().contains("|")
                    ? r.getRutaArchivo().split("\\|")[0].trim()
                    : r.getRutaArchivo().trim()))
            .filter(Files::exists)
            .findFirst();
    }

    public boolean eliminarRespaldo(String id) {
        // FIX: buscar el registro y proteger contra rutaArchivo null
        Optional<BackupRecord> rec = historial.stream()
            .filter(r -> r.getId().equals(id))
            .findFirst();
        if (rec.isEmpty()) return false;

        String ruta = rec.get().getRutaArchivo();
        if (ruta != null && !ruta.isBlank()) {
            // Si es un respaldo completo, tiene dos rutas separadas por |
            for (String parte : ruta.split("\\|")) {
                String p = parte.trim();
                if (!p.isEmpty()) {
                    try { Files.deleteIfExists(Paths.get(p)); }
                    catch (IOException e) { log.warn("No se pudo borrar: {}", p); }
                }
            }
        }
        historial.removeIf(r -> r.getId().equals(id));
        return true;
    }

    // ── Implementación interna ─────────────────────────────────

    /**
     * FIX: usar @Transactional(readOnly=true) para que JPA no intente
     * hacer commit al final — eso causaba "Unable to commit against JDBC Connection"
     * cuando el pooler de Supabase cerraba la conexión entre las queries.
     */
    @Transactional(readOnly = true)
    public BackupRecord respaldarPostgres(String origen) {
        String id            = "PG-" + LocalDateTime.now().format(TS);
        String nombreArchivo = "bruma_postgres_" + LocalDateTime.now().format(TS) + ".json";
        Path   ruta          = Paths.get(directorioBase, nombreArchivo);
        LocalDateTime ahora  = LocalDateTime.now();

        try {
            StringBuilder sb = new StringBuilder();
            sb.append("{\n");
            sb.append("  \"exportado\": \"").append(ahora).append("\",\n");
            sb.append("  \"origen\": \"").append(origen).append("\",\n");
            sb.append("  \"base_datos\": \"PostgreSQL / Supabase\",\n");

            // ── Clientes ──────────────────────────────────────
            var clientes = clienteRepo.findAll();
            sb.append("  \"clientes\": [\n");
            for (int i = 0; i < clientes.size(); i++) {
                var c = clientes.get(i);
                sb.append("    {")
                  .append("\"id\":").append(c.getIdCliente()).append(",")
                  .append("\"nombre\":\"").append(esc(c.getNombre())).append("\",")
                  .append("\"dni\":\"").append(esc(c.getDni())).append("\",")
                  .append("\"fechaRegistro\":\"").append(c.getFechaRegistro()).append("\"")
                  .append("}");
                if (i < clientes.size() - 1) sb.append(",");
                sb.append("\n");
            }
            sb.append("  ],\n");

            // ── Productos ─────────────────────────────────────
            var productos = productoRepo.findAll();
            sb.append("  \"productos\": [\n");
            for (int i = 0; i < productos.size(); i++) {
                var p = productos.get(i);
                sb.append("    {")
                  .append("\"id\":").append(p.getIdProducto()).append(",")
                  .append("\"nombre\":\"").append(esc(p.getNombre())).append("\",")
                  .append("\"precio\":").append(p.getPrecio()).append(",")
                  .append("\"categoria\":\"").append(esc(p.getCategoria())).append("\",")
                  .append("\"descripcion\":\"").append(esc(p.getDescripcion())).append("\"")
                  .append("}");
                if (i < productos.size() - 1) sb.append(",");
                sb.append("\n");
            }
            sb.append("  ],\n");

            // ── Pedidos ───────────────────────────────────────
            var pedidos = pedidoRepo.findAllWithCliente();
            sb.append("  \"pedidos\": [\n");
            for (int i = 0; i < pedidos.size(); i++) {
                var p = pedidos.get(i);
                sb.append("    {")
                  .append("\"id\":").append(p.getIdPedido()).append(",")
                  .append("\"fechaPedido\":\"").append(p.getFechaPedido()).append("\",")
                  .append("\"total\":").append(p.getTotal()).append(",")
                  .append("\"comentario\":\"").append(esc(p.getComentario())).append("\",")
                  .append("\"idCliente\":").append(
                      p.getCliente() != null ? p.getCliente().getIdCliente() : "null")
                  .append("}");
                if (i < pedidos.size() - 1) sb.append(",");
                sb.append("\n");
            }
            sb.append("  ],\n");

            // ── Usuarios (sin password) ───────────────────────
            var usuarios = usuarioRepo.findAll();
            sb.append("  \"usuarios\": [\n");
            for (int i = 0; i < usuarios.size(); i++) {
                var u = usuarios.get(i);
                sb.append("    {")
                  .append("\"id\":").append(u.getId()).append(",")
                  .append("\"username\":\"").append(esc(u.getUsername())).append("\",")
                  .append("\"nombre\":\"").append(esc(u.getNombre())).append("\",")
                  .append("\"rol\":\"").append(esc(u.getRol())).append("\"")
                  .append("}");
                if (i < usuarios.size() - 1) sb.append(",");
                sb.append("\n");
            }
            sb.append("  ]\n}\n");

            Files.writeString(ruta, sb.toString());
            long size = Files.size(ruta);
            log.info("Respaldo PostgreSQL OK: {} ({} bytes)", nombreArchivo, size);

            BackupRecord rec = new BackupRecord(id, BackupRecord.Tipo.POSTGRESQL,
                BackupRecord.Estado.OK, ahora, ruta.toString(), size, null);
            agregarHistorial(rec);
            return rec;

        } catch (Exception e) {
            log.error("Error respaldo PostgreSQL: {}", e.getMessage(), e);
            // Limpiar archivo parcial si existe
            try { Files.deleteIfExists(ruta); } catch (IOException ignored) {}

            BackupRecord rec = new BackupRecord(id, BackupRecord.Tipo.POSTGRESQL,
                BackupRecord.Estado.ERROR, ahora, null, 0,
                e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName());
            agregarHistorial(rec);
            return rec;
        }
    }

    public BackupRecord respaldarMongo(String origen) {
        String id            = "MG-" + LocalDateTime.now().format(TS);
        String nombreArchivo = "bruma_mongo_" + LocalDateTime.now().format(TS) + ".json";
        Path   ruta          = Paths.get(directorioBase, nombreArchivo);
        LocalDateTime ahora  = LocalDateTime.now();

        try {
            var infos = infoRepo.findAll();

            StringBuilder sb = new StringBuilder();
            sb.append("{\n");
            sb.append("  \"exportado\": \"").append(ahora).append("\",\n");
            sb.append("  \"origen\": \"").append(origen).append("\",\n");
            sb.append("  \"base_datos\": \"MongoDB Atlas / bruma_db\",\n");
            sb.append("  \"coleccion\": \"cliente_info\",\n");
            sb.append("  \"total_documentos\": ").append(infos.size()).append(",\n");
            sb.append("  \"documentos\": [\n");

            for (int i = 0; i < infos.size(); i++) {
                var ci = infos.get(i);
                sb.append("    {\n");
                sb.append("      \"idCliente\": ").append(ci.getIdCliente()).append(",\n");

                var pref = ci.getPreferencias();
                sb.append("      \"preferencias\": {\n");
                sb.append("        \"metodoPago\": \"").append(esc(pref.getMetodoPago())).append("\",\n");
                sb.append("        \"notificaciones\": ").append(pref.getNotificaciones()).append("\n");
                sb.append("      },\n");

                var comentarios = ci.getComentarios();
                sb.append("      \"comentarios\": [\n");
                for (int j = 0; j < comentarios.size(); j++) {
                    var com = comentarios.get(j);
                    sb.append("        {")
                      .append("\"texto\":\"").append(esc(com.getTexto())).append("\",")
                      .append("\"fecha\":\"").append(esc(com.getFecha())).append("\"")
                      .append("}");
                    if (j < comentarios.size() - 1) sb.append(",");
                    sb.append("\n");
                }
                sb.append("      ]\n");
                sb.append("    }");
                if (i < infos.size() - 1) sb.append(",");
                sb.append("\n");
            }
            sb.append("  ]\n}\n");

            Files.writeString(ruta, sb.toString());
            long size = Files.size(ruta);
            log.info("Respaldo MongoDB OK: {} ({} bytes)", nombreArchivo, size);

            BackupRecord rec = new BackupRecord(id, BackupRecord.Tipo.MONGODB,
                BackupRecord.Estado.OK, ahora, ruta.toString(), size, null);
            agregarHistorial(rec);
            return rec;

        } catch (Exception e) {
            log.error("Error respaldo MongoDB: {}", e.getMessage(), e);
            try { Files.deleteIfExists(ruta); } catch (IOException ignored) {}

            BackupRecord rec = new BackupRecord(id, BackupRecord.Tipo.MONGODB,
                BackupRecord.Estado.ERROR, ahora, null, 0,
                e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName());
            agregarHistorial(rec);
            return rec;
        }
    }

    // ── Utilidades ─────────────────────────────────────────────

    private void agregarHistorial(BackupRecord rec) {
        historial.add(0, rec);
    }

    private void limpiarRespaldosAntiguos() {
        if (historial.size() <= MAX_RESPALDOS) return;
        List<BackupRecord> sobran = new ArrayList<>(historial.subList(MAX_RESPALDOS, historial.size()));
        for (BackupRecord r : sobran) {
            if (r.getRutaArchivo() != null) {
                for (String parte : r.getRutaArchivo().split("\\|")) {
                    String p = parte.trim();
                    if (!p.isEmpty()) {
                        try { Files.deleteIfExists(Paths.get(p)); } catch (IOException ignored) {}
                    }
                }
            }
        }
        if (historial.size() > MAX_RESPALDOS)
            historial.subList(MAX_RESPALDOS, historial.size()).clear();
    }

    private void cargarHistorialDesconocido(Path dir) {
        try (var stream = Files.list(dir)) {
            stream.filter(p -> p.toString().endsWith(".json"))
                  .sorted(Comparator.reverseOrder())
                  .limit(MAX_RESPALDOS)
                  .forEach(p -> {
                      String nombre = p.getFileName().toString();
                      BackupRecord.Tipo tipo = nombre.contains("postgres")
                          ? BackupRecord.Tipo.POSTGRESQL
                          : nombre.contains("mongo")
                              ? BackupRecord.Tipo.MONGODB
                              : BackupRecord.Tipo.COMPLETO;
                      try {
                          historial.add(new BackupRecord(
                              nombre, tipo, BackupRecord.Estado.OK,
                              Files.getLastModifiedTime(p).toInstant()
                                   .atZone(java.time.ZoneId.systemDefault()).toLocalDateTime(),
                              p.toString(), Files.size(p), null
                          ));
                      } catch (IOException ignored) {}
                  });
        } catch (IOException e) {
            log.warn("No se pudo cargar historial previo: {}", e.getMessage());
        }
    }

    private String esc(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\n", "\\n").replace("\r", "");
    }
}
