package com.bruma.service;

import com.bruma.model.mongo.ClienteInfo;
import com.bruma.model.pg.Cliente;
import com.bruma.repository.ClienteInfoRepository;
import com.bruma.repository.ClienteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class ClienteService {

    private final ClienteRepository     clienteRepo;
    private final ClienteInfoRepository infoRepo;

    public ClienteService(ClienteRepository clienteRepo, ClienteInfoRepository infoRepo) {
        this.clienteRepo = clienteRepo;
        this.infoRepo    = infoRepo;
    }

    // FIX #3: orden estable por ID tras cualquier UPDATE
    public List<Cliente> listar()                      { return clienteRepo.findAllByOrderByIdClienteAsc(); }
    public Optional<Cliente> buscarPorId(Integer id)   { return clienteRepo.findById(id); }
    public Optional<Cliente> buscarPorDni(String dni)  { return clienteRepo.findByDni(dni); }

    /**
     * Si el DNI ya existe devuelve ese cliente; si no, lo crea automáticamente
     * junto con su perfil en MongoDB. Usado en el flujo de creación de pedidos.
     */
    @Transactional
    public Cliente buscarOCrearPorDni(String dni, String nombre) {
        return clienteRepo.findByDni(dni).orElseGet(() -> {
            Cliente nuevo = new Cliente();
            nuevo.setDni(dni);
            nuevo.setNombre(nombre);
            return guardar(nuevo);
        });
    }

    @Transactional
    public Cliente guardar(Cliente c) {
        if (c.getFechaRegistro() == null) c.setFechaRegistro(LocalDate.now());
        Cliente saved = clienteRepo.save(c);
        // Crear perfil MongoDB si no existe
        if (infoRepo.findByIdCliente(saved.getIdCliente()).isEmpty()) {
            ClienteInfo info = new ClienteInfo();
            info.setIdCliente(saved.getIdCliente());
            infoRepo.save(info);
        }
        return saved;
    }

    @Transactional
    public void eliminar(Integer id) {
        infoRepo.deleteByIdCliente(id);
        clienteRepo.deleteById(id);
    }

    // ── MongoDB helpers ─────────────────────────────────────────

    public Optional<ClienteInfo> getInfo(Integer idCliente) {
        return infoRepo.findByIdCliente(idCliente);
    }

    @Transactional
    public void agregarComentario(Integer idCliente, String texto) {
        ClienteInfo info = infoRepo.findByIdCliente(idCliente).orElseGet(() -> {
            ClienteInfo ni = new ClienteInfo(); ni.setIdCliente(idCliente); return ni;
        });
        ClienteInfo.Comentario c = new ClienteInfo.Comentario();
        c.setTexto(texto);
        c.setFecha(LocalDate.now().toString());
        info.getComentarios().add(c);
        infoRepo.save(info);
    }

    @Transactional
    public void guardarPreferencias(Integer idCliente, String metodoPago) {
        ClienteInfo info = infoRepo.findByIdCliente(idCliente).orElseGet(() -> {
            ClienteInfo ni = new ClienteInfo(); ni.setIdCliente(idCliente); return ni;
        });
        info.getPreferencias().setMetodoPago(metodoPago);
        infoRepo.save(info);
    }
}
