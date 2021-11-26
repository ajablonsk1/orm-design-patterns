package orm.sql;

import java.util.LinkedList;
import java.util.List;

public class UpdateRecordsSqlBuilder extends SqlBuilder{

    private List<String> tables = new LinkedList<>();

    private List<String> sets = new LinkedList<>();

    private List<String> wheres = new LinkedList<>();

    public UpdateRecordsSqlBuilder(){};

    public UpdateRecordsSqlBuilder table(String tableName){
        this.tables.add(tableName);
        return this;
    }

    public UpdateRecordsSqlBuilder set(String columnName, String value){
        this.sets.add(columnName + " = " + value);
        return this;
    }

    public UpdateRecordsSqlBuilder where(String condition){
        this.wheres.add(condition);
        return this;
    }

    public UpdateRecordsSqlBuilder and(String condition){
        this.wheres.add(condition);
        return this;
    }

    @Override
    public String toString(){
        StringBuilder sql = new StringBuilder("UPDATE");
        addStatementToQuery(sql, tables, " ", ", ")
                .addStatementToQuery(sql, sets, " SET ", ", ")
                .addStatementToQuery(sql, wheres, " WHERE ", " AND ");
        sql.append(";");

        tables.clear();
        sets.clear();
        wheres.clear();

        return sql.toString();
    }
}
