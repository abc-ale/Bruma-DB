package com.bruma.repository;

import com.bruma.model.pg.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Integer> {

    // FIX #5: LEFT JOIN FETCH para incluir pedidos anónimos (cliente == null).
    // El JOIN FETCH anterior los excluía y reventaba Reportes y Dashboard.
    @Query("SELECT p FROM Pedido p LEFT JOIN FETCH p.cliente ORDER BY p.fechaPedido DESC")
    List<Pedido> findAllWithCliente();

    @Query("SELECT p FROM Pedido p LEFT JOIN FETCH p.cliente ORDER BY p.fechaPedido DESC LIMIT 5")
    List<Pedido> findTop5();

    // FIX #6: Carga detalles + producto en una sola query para evitar
    // LazyInitializationException cuando Thymeleaf accede a pedido.detalles.
    @Query("""
           SELECT DISTINCT p FROM Pedido p
           LEFT JOIN FETCH p.cliente
           LEFT JOIN FETCH p.detalles d
           LEFT JOIN FETCH d.producto
           WHERE p.idPedido = :id
           """)
    Optional<Pedido> findByIdWithDetalles(@Param("id") Integer id);
}
