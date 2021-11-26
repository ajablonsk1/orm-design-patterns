package orm.sql;

import java.util.LinkedList;
import java.util.List;

public class InsertRecordsSqlBuilder extends SqlBuilder{

    private List<String> tables = new LinkedList<>();

    private List<String> values = new LinkedList<>();

    public InsertRecordsSqlBuilder(){};

    public InsertRecordsSqlBuilder table(String tableName){
        this.tables.add(tableName);
        return this;
    }

    public InsertRecordsSqlBuilder values(String values){
        this.values.add(values);
        return this;
    }

    @Override
    public String toString(){
        StringBuilder sql = new StringBuilder("INSERT INTO");
        addStatementToQuery(sql, tables, " ", ", ")
                .addStatementToQuery(sql, values, " VALUES(", ", ");
        sql.append(");");

        tables.clear();
        values.clear();

        return sql.toString();
    }
}
