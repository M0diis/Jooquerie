package me.m0dii.jooquerie.dsl.repository;

import me.m0dii.jooquerie.dsl.EntityManager;
import org.jooq.Condition;
import org.jooq.Field;
import org.jooq.Table;

import java.lang.reflect.Method;
import java.util.Map;

public class QueryMethodHandler {
    private static final Map<String, QueryMethod> METHOD_PREFIXES = Map.of(
            "findBy", QueryMethod.FIND,
            "countBy", QueryMethod.COUNT,
            "existsBy", QueryMethod.EXISTS,
            "deleteBy", QueryMethod.DELETE,
            "findAll", QueryMethod.FIND_ALL
    );

    enum QueryMethod {
        FIND, COUNT, EXISTS, DELETE, FIND_ALL
    }

    public static <T> Object handleQueryMethod(EntityManager entityManager,
                                               Class<T> entityClass,
                                               Method method,
                                               Object[] args) {
        String methodName = method.getName();

        for (Map.Entry<String, QueryMethod> entry : METHOD_PREFIXES.entrySet()) {
            if (methodName.startsWith(entry.getKey())) {
                String propertyPath = methodName.substring(entry.getKey().length());
                return processQuery(entityManager, entityClass, entry.getValue(), propertyPath, args);
            }
        }

        throw new IllegalArgumentException("Unknown query method: " + methodName);
    }

    private static <T> Object processQuery(EntityManager entityManager,
                                           Class<T> entityClass,
                                           QueryMethod queryMethod,
                                           String propertyPath,
                                           Object[] args) {
        var mapper = entityManager.getMappers().get(entityClass);
        if (mapper == null) {
            throw new IllegalStateException("No mapper registered for " + entityClass.getName());
        }

        if (queryMethod == QueryMethod.FIND_ALL) {
            return entityManager.createQuery(entityClass).getResultList();
        }

        Table<?> table = mapper.getTable();

        String fieldName = camelCaseToSnakeCase(propertyPath);

        Field<?> field = table.field(fieldName);

        if (field == null) {
            field = table.field(fieldName.toLowerCase());
        }

        if (field == null) {
            field = table.field(fieldName.toUpperCase());
        }

        if (field == null) {
            String searchTerm = fieldName.toLowerCase();
            for (Field<?> tableField : table.fields()) {
                if (tableField.getName().toLowerCase().contains(searchTerm)) {
                    field = tableField;
                    break;
                }
            }
        }

        if (field == null) {
            throw new IllegalArgumentException("Could not find a field matching '" +
                    propertyPath + "' in table " + table.getName());
        }

        Condition condition = ((Field) field).eq(args[0]);

        return switch (queryMethod) {
            case FIND -> entityManager.createQuery(entityClass)
                    .where(condition)
                    .getResultList();
            case COUNT -> entityManager.createQuery(entityClass)
                    .where(condition)
                    .getResultList().size();
            case EXISTS -> !entityManager.createQuery(entityClass)
                    .where(condition)
                    .getResultList().isEmpty();
            case DELETE -> {
                var recordsToDelete = entityManager.createQuery(entityClass)
                        .where(condition)
                        .getResultList();
                for (T entity : recordsToDelete) {
                    entityManager.remove(entity);
                }
                yield recordsToDelete.size();
            }
            default -> throw new IllegalStateException("Unexpected value: " + queryMethod);
        };
    }

    private static String camelCaseToSnakeCase(String camelCase) {
        if (camelCase.isEmpty()) {
            return "";
        }

        String result = camelCase.substring(0, 1).toLowerCase() + camelCase.substring(1);

        result = result.replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase();

        return result;
    }
}