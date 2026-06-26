package com.bruma.repository;

import com.bruma.model.pg.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Integer> {
    Optional<Cliente> findByDni(String dni);
    // FIX #3: orden estable por ID tras cualquier UPDATE
    List<Cliente> findAllByOrderByIdClienteAsc();
}
