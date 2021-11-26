package orm.sql;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class DropTableSqlBuilder extends SqlBuilder{

    private List<String> tables = new LinkedList<>();

    public DropTableSqlBuilder(){};

    public DropTableSqlBuilder table(String tableName){
        this.tables.add(tableName);
        return this;
    }

    @Override
    public String toString(){
        StringBuilder sql = new StringBuilder("DROP TABLE");
        addStatementToQuery(sql, tables, " ", ", ");
        sql.append(";");
        return sql.toString();
    }
}
