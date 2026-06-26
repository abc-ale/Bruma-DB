package com.bruma.repository.mongo;

import com.bruma.model.mongo.ClienteInfo;
import com.bruma.repository.ClienteInfoRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Implementación real de ClienteInfoRepository usando MongoDB Atlas.
 * Solo se activa cuando el perfil "demo" NO está activo.
 */
@Repository
@Profile("!demo")
public interface MongoClienteInfoRepository
        extends ClienteInfoRepository, MongoRepository<ClienteInfo, String> {

    @Override
    Optional<ClienteInfo> findByIdCliente(Integer idCliente);

    @Override
    void deleteByIdCliente(Integer idCliente);
}
