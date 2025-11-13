# Venture

Plataforma empresarial construida sobre Java 21/Spring Boot 3 y Vaadin 24 siguiendo arquitectura limpia modularizada por dominios de negocio.

## Requisitos previos
- Java 21 o superior (JDK 21 recomendado mientras Java 25 LTS esté disponible públicamente).
- Maven 3.9+
- Docker (para ejecutar pruebas de integración con Testcontainers)
- Redis (opcional en desarrollo, requerido para sesiones distribuidas en HA)

## Variables de entorno
```bash
export DB_USER=INV
export DB_PASS=INV
export REDIS_HOST=localhost
export REDIS_PORT=6379
export REDIS_PASSWORD=
```

## Ejecución local
```bash
mvn spring-boot:run
```
La aplicación inicia en `http://localhost:8080` y la UI de personas está disponible en `/BswPersonas`.

## Arquitectura
- `com.inventiva.venture.modules.<modulo>`: módulos funcionales (bs, vt, cp, cc, cm, st, rp, co)
  - `domain`: entidades y reglas de negocio.
  - `application`: servicios orquestadores.
  - `infrastructure`: repositorios, integraciones.
  - `presentation`: vistas Vaadin y componentes UI.
- `com.inventiva.venture.ui`: layouts compartidos.
- Migraciones Flyway en `src/main/resources/db/migration`.

## Convenciones de equipo
- Aplicar patrones de arquitectura limpia respetando límites de capa.
- Reutilizar servicios y repositorios existentes antes de crear nuevos.
- Mapear flags `CHAR(1)` a booleanos auxiliares con `@Transient` y callbacks JPA.
- Mantener pruebas unitarias e integración actualizadas.

## Observabilidad
- Actuator expone métricas y health checks.
- `micrometer-registry-prometheus` habilita `/actuator/prometheus` para scraping.

## Alta disponibilidad con Redis
- Spring Session Redis mantiene sesiones compartidas entre nodos.
- Configurar variables `REDIS_HOST/PORT/PASSWORD` en despliegues HA.

## Futuras extensiones
- Completar módulos funcionales restantes (`vt`, `cp`, `cc`, etc.).
- Añadir seguridad (Spring Security + SSO corporativo).
- Integrar pipelines CI/CD con análisis estático y despliegue automatizado.
- Implementar caching avanzado con Redis y control de expiraciones.

## Estándares de código
- Lombok para getters/setters/builders en entidades.
- Validaciones mediante `jakarta.validation` y `Vaadin Binder` en UI.
- Pruebas obligatorias para servicios, repositorios y componentes UI críticos.

## Cómo ejecutar pruebas
```bash
mvn test
```
Este comando levanta automáticamente un contenedor Oracle XE mediante Testcontainers y ejecuta pruebas unitarias, de integración y de UI.
