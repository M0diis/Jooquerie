package me.m0dii.jooquerie.dsl.query;

import me.m0dii.jooquerie.dsl.EntityManager;
import me.m0dii.jooquerie.dsl.EntityMapper;
import org.jooq.*;

import java.lang.Record;
import java.util.ArrayList;
import java.util.List;

public class QueryBuilder<T> {
    private final EntityManager entityManager;
    private final Class<T> entityClass;
    private final EntityMapper<T> mapper;
    private final DSLContext dslContext;
    private final List<Condition> conditions = new ArrayList<>();
    private Integer limit;
    private Integer offset;

    public QueryBuilder(EntityManager entityManager, DSLContext dslContext, Class<T> entityClass, EntityMapper<T> mapper) {
        this.entityManager = entityManager;
        this.dslContext = dslContext;
        this.entityClass = entityClass;
        this.mapper = mapper;
    }

    public QueryBuilder<T> where(Condition condition) {
        conditions.add(condition);
        return this;
    }

    public QueryBuilder<T> limit(int limit) {
        this.limit = limit;
        return this;
    }

    public QueryBuilder<T> offset(int offset) {
        this.offset = offset;
        return this;
    }

    public List<T> getResultList() {
        SelectWhereStep<?> step1 = dslContext.selectFrom(mapper.getTable());

        SelectConditionStep<?> whereStep = null;
        if (!conditions.isEmpty()) {
            Condition[] conditionsArray = conditions.toArray(new Condition[0]);
            whereStep = step1.where(conditionsArray);
        }

        Select<?> finalQuery;

        if (whereStep != null) {
            if (limit != null) {
                finalQuery = offset != null
                        ? whereStep.limit(limit).offset(offset)
                        : whereStep.limit(limit);
            } else {
                finalQuery = offset != null
                        ? whereStep.offset(offset)
                        : whereStep;
            }
        } else {
            if (limit != null) {
                finalQuery = offset != null
                        ? step1.limit(limit).offset(offset)
                        : step1.limit(limit);
            } else {
                finalQuery = offset != null
                        ? step1.offset(offset)
                        : step1;
            }
        }

        Result<?> result = finalQuery.fetch();
        return result.map(mapper::fromRecord);
    }

    public T getSingleResult() {
        List<T> results = limit(1).getResultList();
        return results.isEmpty() ? null : results.getFirst();
    }
}