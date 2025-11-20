package com.inventiva.venture.utils;

import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

public class MapSpecificationBuilder<T> {

    public Specification<T> build(Map<String, Object> filters) {
        return construir(filters);
    }

    /**
     * Método principal para construir la Specification a partir del mapa de
     * filtros.
     */
    public Specification<T> construir(Map<String, Object> filtros) {
        return (root, query, cb) -> {
            List<Predicate> predicados = new ArrayList<>();

            for (Map.Entry<String, Object> entry : filtros.entrySet()) {
                String clave = entry.getKey();
                Object valor = entry.getValue();

                if (valor == null) {
                    continue;
                }
                if (valor instanceof String s && s.isBlank()) {
                    continue;
                }

                Predicate p = construirPredicado(clave, valor, root, cb);
                if (p != null) {
                    predicados.add(p);
                }
            }

            return predicados.isEmpty()
                    ? cb.conjunction()
                    : cb.and(predicados.toArray(new Predicate[0]));
        };
    }

    private Predicate construirPredicado(String clave,
            Object valor,
            Root<T> root,
            CriteriaBuilder cb) {
        // Convención: "operador:propiedad" o solo "propiedad" (default LIKEIC)
        String operador;
        String propiedad;

        String[] partes = clave.split(":", 2);
        if (partes.length == 2) {
            operador = partes[0].toLowerCase();
            propiedad = partes[1];
        } else {
            operador = "likeic"; // default: LIKE ignore case
            propiedad = clave;
        }

        return switch (operador) {
            case "eq" -> { // igualdad
                Path<Object> ruta = resolverRuta(root, propiedad);
                yield cb.equal(ruta, valor);
            }
            case "like" -> { // like normal
                Path<String> ruta = resolverRuta(root, propiedad);
                yield cb.like(ruta, "%" + valor + "%");
            }
            case "likeic" -> { // like ignore case
                Path<String> ruta = resolverRuta(root, propiedad);
                yield cb.like(cb.upper(ruta), "%" + valor.toString().toUpperCase() + "%");
            }
            case "ge" -> { // >=
                Path<?> ruta = resolverRuta(root, propiedad);
                yield construirPredicadoComparablePorTipoJava(cb, ruta, valor, "ge");
            }
            case "le" -> { // <=
                Path<?> ruta = resolverRuta(root, propiedad);
                yield construirPredicadoComparablePorTipoJava(cb, ruta, valor, "le");
            }
            case "gt" -> { // >
                Path<?> ruta = resolverRuta(root, propiedad);
                yield construirPredicadoComparablePorTipoJava(cb, ruta, valor, "gt");
            }
            case "lt" -> { // <
                Path<?> ruta = resolverRuta(root, propiedad);
                yield construirPredicadoComparablePorTipoJava(cb, ruta, valor, "lt");
            }
            case "between" -> { // entre (incluye extremos)
                Path<?> ruta = resolverRuta(root, propiedad);
                yield construirPredicadoBetweenPorTipoJava(cb, ruta, valor);
            }
            case "in" -> { // IN lista
                Path<Object> ruta = resolverRuta(root, propiedad);
                CriteriaBuilder.In<Object> in = cb.in(ruta);
                for (Object v : (Collection<?>) valor) {
                    in.value(v);
                }
                yield in;
            }
            case "orlikeic" -> {
                // propiedad = "campo1,campo2,..."
                String[] campos = propiedad.split(",");
                List<Predicate> ors = new ArrayList<>();
                for (String campo : campos) {
                    Path<String> p = resolverRuta(root, campo.trim());
                    ors.add(cb.like(
                            cb.upper(p),
                            "%" + valor.toString().toUpperCase() + "%"));
                }
                yield cb.or(ors.toArray(new Predicate[0]));
            }
            case "concatlikeic" -> {
                // propiedad = "campo1,campo2,..."
                String[] campos = propiedad.split(",");
                List<Expression<String>> expresiones = new ArrayList<>();
                for (String campo : campos) {
                    Path<String> p = resolverRuta(root, campo.trim());
                    expresiones.add(cb.upper(p));
                }
                Expression<String> concatenado = concatenarExpresiones(cb, expresiones, " ");
                yield cb.like(concatenado, "%" + valor.toString().toUpperCase() + "%");
            }
            default -> null;
        };
    }

    private Expression<String> concatenarExpresiones(CriteriaBuilder cb,
            List<Expression<String>> expresiones,
            String separador) {
        Expression<String> resultado = expresiones.get(0);
        for (int i = 1; i < expresiones.size(); i++) {
            resultado = cb.concat(cb.concat(resultado, separador), expresiones.get(i));
        }
        return resultado;
    }

    /**
     * Soporta paths anidados: "bswPaises.descripcion", "bswPaises.region.nombre",
     * etc.
     */
    @SuppressWarnings("unchecked")
    private <X> Path<X> resolverRuta(From<?, ?> root, String rutaPropiedad) {
        String[] partes = rutaPropiedad.split("\\.");
        From<?, ?> joinRoot = root;
        for (int i = 0; i < partes.length - 1; i++) {
            joinRoot = joinRoot.join(partes[i], JoinType.LEFT);
        }
        return (Path<X>) joinRoot.get(partes[partes.length - 1]);
    }

    /**
     * Construye un predicado para operadores que requieren Comparable
     * (ge, le, gt, lt), detectando el tipo real del atributo a partir
     * de path.getJavaType().
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private Predicate construirPredicadoComparablePorTipoJava(CriteriaBuilder cb,
            Path<?> ruta,
            Object valorBruto,
            String operador) {
        Class<?> tipoJava = ruta.getJavaType();

        // Verificamos que el atributo sea Comparable
        if (!Comparable.class.isAssignableFrom(tipoJava)) {
            throw new IllegalArgumentException(
                    "El atributo '" + ruta + "' de tipo " + tipoJava.getName() +
                            " no implementa Comparable, no se puede usar con '" + operador + "'.");
        }

        // Convertimos el valor del filtro al tipo real del atributo
        Object convertido = convertirValor(valorBruto, tipoJava);
        if (!(convertido instanceof Comparable comparableValor)) {
            throw new IllegalArgumentException(
                    "El valor para '" + operador + "' no es comparable tras la conversión: " + convertido);
        }

        Expression expresion = (Expression) ruta; // Expression<Y>
        Comparable comparable = comparableValor; // Y

        return switch (operador) {
            case "ge" -> cb.greaterThanOrEqualTo(expresion, comparable);
            case "le" -> cb.lessThanOrEqualTo(expresion, comparable);
            case "gt" -> cb.greaterThan(expresion, comparable);
            case "lt" -> cb.lessThan(expresion, comparable);
            default -> throw new IllegalArgumentException("Operador no soportado: " + operador);
        };
    }

    /**
     * Construye un predicado BETWEEN detectando el tipo real del atributo.
     * Convención del valor:
     * - Collection con al menos 2 elementos: [desde, hasta]
     * - Array con al menos 2 elementos: [desde, hasta]
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private Predicate construirPredicadoBetweenPorTipoJava(CriteriaBuilder cb,
            Path<?> ruta,
            Object valorBruto) {
        Class<?> tipoJava = ruta.getJavaType();

        if (!Comparable.class.isAssignableFrom(tipoJava)) {
            throw new IllegalArgumentException(
                    "El atributo '" + ruta + "' de tipo " + tipoJava.getName() +
                            " no implementa Comparable, no se puede usar con 'between'.");
        }

        List<Object> valores = new ArrayList<>();

        if (valorBruto instanceof Collection<?> coleccion) {
            valores.addAll(coleccion);
        } else if (valorBruto != null && valorBruto.getClass().isArray()) {
            Object[] arreglo = (Object[]) valorBruto;
            valores.addAll(Arrays.asList(arreglo));
        } else {
            throw new IllegalArgumentException(
                    "El valor para 'between' debe ser una Collection o un array con al menos 2 elementos.");
        }

        if (valores.size() < 2) {
            throw new IllegalArgumentException(
                    "El valor para 'between' debe contener al menos 2 elementos (desde, hasta).");
        }

        Object desdeBruto = valores.get(0);
        Object hastaBruto = valores.get(1);

        Object desdeConvertido = convertirValor(desdeBruto, tipoJava);
        Object hastaConvertido = convertirValor(hastaBruto, tipoJava);

        if (!(desdeConvertido instanceof Comparable desdeComparable) ||
                !(hastaConvertido instanceof Comparable hastaComparable)) {
            throw new IllegalArgumentException(
                    "Los valores para 'between' no son comparables tras la conversión.");
        }

        Expression expresion = (Expression) ruta; // Expression<Y>
        return cb.between(expresion, desdeComparable, hastaComparable);
    }

    /**
     * Convierte el valor recibido (normalmente String u Object genérico de la API)
     * al tipo real del atributo de la entidad.
     */
    private Object convertirValor(Object valor, Class<?> tipoDestino) {
        if (valor == null) {
            return null;
        }
        // Si ya es del tipo correcto, lo devolvemos tal cual
        if (tipoDestino.isInstance(valor)) {
            return valor;
        }

        // Conversión básica desde String (típico en APIs REST)
        if (valor instanceof String s) {
            s = s.trim();
            if (tipoDestino.equals(String.class)) {
                return s;
            } else if (tipoDestino.equals(Integer.class) || tipoDestino.equals(int.class)) {
                return Integer.valueOf(s);
            } else if (tipoDestino.equals(Long.class) || tipoDestino.equals(long.class)) {
                return Long.valueOf(s);
            } else if (tipoDestino.equals(Double.class) || tipoDestino.equals(double.class)) {
                return Double.valueOf(s);
            } else if (tipoDestino.equals(Float.class) || tipoDestino.equals(float.class)) {
                return Float.valueOf(s);
            } else if (tipoDestino.equals(BigDecimal.class)) {
                return new BigDecimal(s);
            } else if (tipoDestino.equals(LocalDate.class)) {
                // Ajustar formato si tus fechas no vienen en ISO (yyyy-MM-dd)
                return LocalDate.parse(s);
            } else if (tipoDestino.equals(LocalDateTime.class)) {
                // Ajustar formato si tus datetimes no vienen en ISO
                return LocalDateTime.parse(s);
            } else if (tipoDestino.equals(Boolean.class) || tipoDestino.equals(boolean.class)) {
                return Boolean.valueOf(s);
            }            
        }
        // Último recurso: si no sabemos convertir, devolvemos el valor original
        // y dejaremos que falle más adelante si no es compatible.
        return valor;
    }
}
