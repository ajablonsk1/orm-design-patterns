package orm.sql;

import java.util.LinkedList;
import java.util.List;

public class InsertRecordSqlBuilder extends SqlBuilder{

    private List<String> tables = new LinkedList<>();

    private List<String> values = new LinkedList<>();

    public InsertRecordSqlBuilder(){};

    public InsertRecordSqlBuilder table(String tableName){
        this.tables.add(tableName);
        return this;
    }

    public InsertRecordSqlBuilder values(String values){
        this.values.add(values);
        return this;
    }

    @Override
    public String toString(){
        StringBuilder sql = new StringBuilder("INSERT INTO");
        addStatementToQuery(sql, tables, " ", ", ")
                .addStatementToQuery(sql, values, " VALUES(", ", ");
        sql.append(");");
        return sql.toString();
    }
}
