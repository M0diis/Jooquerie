package me.m0dii.jooquerie.generated.tables;

import me.m0dii.jooquerie.generated.records.UsersRecord;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.Table;
import org.jooq.TableField;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;
import org.jooq.impl.TableImpl;

public class Users extends TableImpl<UsersRecord> {
    public static final Users USERS = new Users();

    public final TableField<UsersRecord, Long> ID = createField(DSL.name("ID"), SQLDataType.BIGINT.notNull(), this);
    public final TableField<UsersRecord, String> USERNAME = createField(DSL.name("USERNAME"), SQLDataType.VARCHAR(100), this);
    public final TableField<UsersRecord, String> EMAIL = createField(DSL.name("EMAIL"), SQLDataType.VARCHAR(100), this);

    public Users() {
        super(DSL.name("USERS"));
    }

    @Override
    public Class<UsersRecord> getRecordType() {
        return UsersRecord.class;
    }
}