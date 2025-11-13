# Venture Agent Guidelines

## Reglas de arquitectura
- Mantener la arquitectura limpia por capas (domain, application, infrastructure, presentation) dentro de cada módulo.
- Reutilizar servicios y repositorios compartidos en el módulo `bs` antes de duplicar lógica.
- Colocar configuraciones transversales fuera de los módulos en paquetes dedicados bajo `com.inventiva.venture`.

## Convenciones de nombres
- Usar `UpperCamelCase` para clases y `lowerCamelCase` para métodos y propiedades.
- Nombrar servicios con el sufijo `Service`, repositorios con `Repository` y componentes Vaadin con `View` o `Form` según corresponda.

## Guía de nuevos módulos
1. Crear carpeta dentro de `src/main/java/com/inventiva/venture/modules/<modulo>`.
2. Generar subcarpetas `domain`, `application`, `infrastructure`, `presentation`.
3. Añadir un `README.md` describiendo el objetivo y dependencias.
4. Incluir pruebas unitarias e integración relevantes en `src/test/java` replicando la estructura del módulo.

## Flags `CHAR(1)` a booleanos
- Mapear campos `CHAR(1)` en entidades JPA como `String` y añadir un campo auxiliar `boolean` anotado con `@Transient`.
- Utilizar callbacks `@PostLoad`, `@PrePersist` y `@PreUpdate` para sincronizar valores entre el campo de base de datos y el booleano.
- Interpretar `'S'` o `'A'` como `true` y `'N'` o `'I'` como `false`.

## Checklist de PR
- [ ] Código compilado y pruebas ejecutadas.
- [ ] Cobertura relevante para servicios y repositorios.
- [ ] Validaciones y Binder de Vaadin revisados.
- [ ] Documentación actualizada (README, notas de módulo, etc.).
- [ ] Configuraciones sensibles extraídas a variables de entorno.

## Buenas prácticas Vaadin
- Utilizar `Binder` para validaciones y sincronización de datos en formularios.
- Preferir `CallbackDataProvider` para grids con paginado server-side.
- Mantener componentes UI desacoplados de la lógica de negocio utilizando servicios.

## Buenas prácticas JPA
- Evitar `fetch` ansioso innecesario; usar `LAZY` por defecto.
- Definir secuencias y generación de IDs explícitamente.
- Documentar y versionar cambios de base de datos con Flyway.

## Buenas prácticas de servicios
- Inyectar dependencias a través de constructores.
- Encapsular transacciones en la capa de aplicación o servicio.
- Manejar excepciones específicas y traducirlas a mensajes de dominio comprensibles.
