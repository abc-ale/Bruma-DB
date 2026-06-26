package com.bruma.repository;

import com.bruma.model.mongo.ClienteInfo;

import java.util.List;
import java.util.Optional;

/**
 * Interfaz de acceso a datos de ClienteInfo.
 * Hay dos implementaciones:
 *  - MongoClienteInfoRepository (perfil por defecto / producción)
 *  - NoOpClienteInfoRepository  (perfil "demo", sin MongoDB)
 */
public interface ClienteInfoRepository {
    Optional<ClienteInfo> findByIdCliente(Integer idCliente);
    void deleteByIdCliente(Integer idCliente);
    <S extends ClienteInfo> S save(S entity);
    List<ClienteInfo> findAll();
    Optional<ClienteInfo> findById(String id);
    void deleteById(String id);
    long count();
    boolean existsById(String id);
    void deleteAll();
    <S extends ClienteInfo> List<S> saveAll(Iterable<S> entities);
    <S extends ClienteInfo> List<S> insert(Iterable<S> entities);
    <S extends ClienteInfo> S insert(S entity);
}
