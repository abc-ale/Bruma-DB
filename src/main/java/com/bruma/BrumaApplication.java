package com.bruma;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Punto de entrada de BRUMA Cafetería.
 *
 * Se separan los repositorios JPA y MongoDB en distintos paquetes
 * para que Spring no intente registrar MongoRepository como JPA ni viceversa.
 *
 *   Producción: --spring.profiles.active=  (vacío, usa application.properties)
 *   Demo:       --spring.profiles.active=demo
 */
@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.bruma.repository")
@EnableMongoRepositories(basePackages = "com.bruma.repository.mongo")
@EnableScheduling
public class BrumaApplication {
    public static void main(String[] args) {
        SpringApplication.run(BrumaApplication.class, args);
    }
}

//http://localhost:8081