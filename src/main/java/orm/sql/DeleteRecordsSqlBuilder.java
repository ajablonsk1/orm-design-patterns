package orm.sql;

import java.util.LinkedList;
import java.util.List;
import java.util.WeakHashMap;

public class DeleteRecordsSqlBuilder extends SqlBuilder{

    private List<String> tables = new LinkedList<>();

    private List<String> wheres = new LinkedList<>();

    public DeleteRecordsSqlBuilder(){};

    public DeleteRecordsSqlBuilder table(String tableName) {
        this.tables.add(tableName);
        return this;
    }
    public DeleteRecordsSqlBuilder where(String condition){
        this.wheres.add(condition);
        return this;
    }

    public DeleteRecordsSqlBuilder and(String condition){
        this.wheres.add(condition);
        return this;
    }

    @Override
    public String toString(){
        StringBuilder sql = new StringBuilder("DELETE");
        addStatementToQuery(sql, tables, " FROM ", ", ")
                .addStatementToQuery(sql, wheres, " WHERE ", " AND ");
        sql.append(";");

        tables.clear();
        wheres.clear();

        return sql.toString();
    }
}
