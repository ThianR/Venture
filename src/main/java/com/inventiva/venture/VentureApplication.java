package com.inventiva.venture;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import io.github.cdimascio.dotenv.Dotenv;

@SuppressWarnings("PMD.UseUtilityClass")
@SpringBootApplication
public class VentureApplication {

    public static void main(String[] args) {
        // Carga automática del archivo .env
        Dotenv dotenv = Dotenv.configure()
                .ignoreIfMissing()
                .load();

        // Exporta las variables al sistema (solo runtime)
        dotenv.entries().forEach(entry -> System.setProperty(entry.getKey(), entry.getValue()));
        SpringApplication.run(VentureApplication.class, args);
    }
}
