package me.m0dii.jooquerie.dsl.repository;

import me.m0dii.jooquerie.dsl.EntityManager;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Proxy;

public class RepositoryProxyFactory {

    private RepositoryProxyFactory() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    @SuppressWarnings("unchecked")
    public static <T, R extends Repository<T, ?>> R createRepository(EntityManager entityManager,
                                                                     Class<R> repositoryInterface) {
        ParameterizedType type = (ParameterizedType) repositoryInterface.getGenericInterfaces()[0];
        Class<T> entityClass = (Class<T>) type.getActualTypeArguments()[0];

        return (R) Proxy.newProxyInstance(
                repositoryInterface.getClassLoader(),
                new Class<?>[]{repositoryInterface},
                new RepositoryInvocationHandler<>(entityManager, entityClass)
        );
    }

    private static class RepositoryInvocationHandler<T> implements InvocationHandler {
        private final EntityManager entityManager;
        private final Class<T> entityClass;
        private final Repository<T, Object> defaultImplementation;

        public RepositoryInvocationHandler(EntityManager entityManager, Class<T> entityClass) {
            this.entityManager = entityManager;
            this.entityClass = entityClass;
            this.defaultImplementation = new AbstractRepository<>(entityManager, entityClass) {
            };
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (method.getDeclaringClass() == Repository.class) {
                return method.invoke(defaultImplementation, args);
            }

            if (method.getDeclaringClass() == Object.class) {
                return method.invoke(this, args);
            }

            return QueryMethodHandler.handleQueryMethod(entityManager, entityClass, method, args);
        }
    }
}