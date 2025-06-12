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
            "findAll", QueryMethod.FIND_ALL,
            "findLikeBy", QueryMethod.FIND_LIKE
    );

    enum QueryMethod {
        FIND, COUNT, EXISTS, DELETE, FIND_ALL, FIND_LIKE
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

        String[] propertyNames = splitCamelCaseByAnd(propertyPath);

        if (propertyNames.length != args.length) {
            throw new IllegalArgumentException("Number of method parameters (" + args.length +
                    ") doesn't match number of property names (" + propertyNames.length + ")");
        }

        Condition condition = null;

        for (int i = 0; i < propertyNames.length; i++) {
            String fieldName = camelCaseToSnakeCase(propertyNames[i]);

            Field<?> field = resolveField(table, fieldName);

            if (field == null) {
                throw new IllegalArgumentException("Could not find a field matching '" +
                        propertyNames[i] + "' in table " + table.getName());
            }

            Condition nextCondition;
            if (queryMethod == QueryMethod.FIND_LIKE) {
                String likePattern = "%" + args[i] + "%";
                nextCondition = field.like(likePattern);
            } else {
                nextCondition = ((Field) field).eq(args[i]);
            }

            condition = (condition == null) ? nextCondition : condition.and(nextCondition);
        }

        return switch (queryMethod) {
            // Rest of switch cases remains the same
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
            case FIND_LIKE -> {
                yield entityManager.createQuery(entityClass)
                        .where(condition)
                        .getResultList();
            }
            default -> throw new IllegalStateException("Unexpected value: " + queryMethod);
        };
    }

    private static Field<?> resolveField(Table<?> table, String fieldName) {
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

        return field;
    }

    private static String[] splitCamelCaseByAnd(String input) {
        if (!input.contains("And")) {
            return new String[] { input };
        }

        return input.split("And");
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