package com.bruma.backup;

import org.springframework.core.io.PathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.nio.file.Path;
import java.util.Optional;

/**
 * Controlador del panel de respaldos.
 * Solo accesible para ROLE_ADMIN.
 */
@Controller
@RequestMapping("/respaldos")
@PreAuthorize("hasRole('ADMIN')")
public class BackupController {

    private final BackupService backupService;

    public BackupController(BackupService backupService) {
        this.backupService = backupService;
    }

    /** Panel principal — lista el historial de respaldos */
    @GetMapping
    public String panel(Model model) {
        model.addAttribute("respaldos", backupService.obtenerHistorial());
        return "respaldos";
    }

    /** Generar respaldo de PostgreSQL manualmente */
    @PostMapping("/ejecutar/postgres")
    public String ejecutarPostgres(RedirectAttributes ra) {
        BackupRecord rec = backupService.ejecutarRespaldoPostgres();
        if (rec.getEstado() == BackupRecord.Estado.OK) {
            ra.addFlashAttribute("mensajeOk",
                "✅ Respaldo PostgreSQL generado: " + rec.getNombreArchivo()
                + " (" + rec.getTamanioFormateado() + ")");
        } else {
            ra.addFlashAttribute("mensajeError",
                "❌ Error al generar respaldo PostgreSQL: " + rec.getMensajeError());
        }
        return "redirect:/respaldos";
    }

    /** Generar respaldo de MongoDB manualmente */
    @PostMapping("/ejecutar/mongo")
    public String ejecutarMongo(RedirectAttributes ra) {
        BackupRecord rec = backupService.ejecutarRespaldoMongo();
        if (rec.getEstado() == BackupRecord.Estado.OK) {
            ra.addFlashAttribute("mensajeOk",
                "✅ Respaldo MongoDB generado: " + rec.getNombreArchivo()
                + " (" + rec.getTamanioFormateado() + ")");
        } else {
            ra.addFlashAttribute("mensajeError",
                "❌ Error al generar respaldo MongoDB: " + rec.getMensajeError());
        }
        return "redirect:/respaldos";
    }

    /** Generar respaldo completo (PG + Mongo) manualmente */
    @PostMapping("/ejecutar/completo")
    public String ejecutarCompleto(RedirectAttributes ra) {
        BackupRecord rec = backupService.ejecutarRespaldoCompleto("MANUAL");
        if (rec.getEstado() == BackupRecord.Estado.OK) {
            ra.addFlashAttribute("mensajeOk",
                "✅ Respaldo completo generado (" + rec.getTamanioFormateado() + ")");
        } else {
            ra.addFlashAttribute("mensajeError",
                "❌ Respaldo completo con errores: " + rec.getMensajeError());
        }
        return "redirect:/respaldos";
    }

    /** Descargar archivo de respaldo */
    @GetMapping("/descargar/{id}")
    public ResponseEntity<Resource> descargar(@PathVariable String id) {
        Optional<Path> ruta = backupService.obtenerRuta(id);
        if (ruta.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Resource resource = new PathResource(ruta.get());
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"" + ruta.get().getFileName() + "\"")
            .contentType(MediaType.APPLICATION_JSON)
            .body(resource);
    }

    /** Eliminar un respaldo del historial y disco */
    @PostMapping("/eliminar/{id}")
    public String eliminar(@PathVariable String id, RedirectAttributes ra) {
        boolean ok = backupService.eliminarRespaldo(id);
        if (ok) {
            ra.addFlashAttribute("mensajeOk", "🗑 Respaldo eliminado correctamente.");
        } else {
            ra.addFlashAttribute("mensajeError", "No se encontró el respaldo con id: " + id);
        }
        return "redirect:/respaldos";
    }
}
