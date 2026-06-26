package com.bruma.repository;

import com.bruma.model.mongo.ClienteInfo;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Implementación vacía de ClienteInfoRepository para el perfil "demo".
 * No realiza ninguna operación real — evita toda conexión a MongoDB.
 */
@Repository
@Profile("demo")
public class NoOpClienteInfoRepository implements ClienteInfoRepository {

    @Override public Optional<ClienteInfo> findByIdCliente(Integer idCliente) { return Optional.empty(); }
    @Override public void deleteByIdCliente(Integer idCliente) {}
    @Override public <S extends ClienteInfo> S save(S entity) { return entity; }
    @Override public List<ClienteInfo> findAll() { return List.of(); }
    @Override public Optional<ClienteInfo> findById(String id) { return Optional.empty(); }
    @Override public void deleteById(String id) {}
    @Override public long count() { return 0; }
    @Override public boolean existsById(String id) { return false; }
    @Override public void deleteAll() {}
    @Override public <S extends ClienteInfo> List<S> saveAll(Iterable<S> entities) { return List.of(); }
    @Override public <S extends ClienteInfo> List<S> insert(Iterable<S> entities) { return List.of(); }
    @Override public <S extends ClienteInfo> S insert(S entity) { return entity; }
}
