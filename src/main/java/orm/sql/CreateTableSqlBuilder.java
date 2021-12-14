package orm.sql;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

public class CreateTableSqlBuilder extends SqlBuilder {

    private List<String> tables = new LinkedList<>();

    private List<String> columns = new LinkedList<>();


    public CreateTableSqlBuilder(){}

    public CreateTableSqlBuilder table(String tableName){
        this.tables.add(tableName);
        return this;
    }
    public CreateTableSqlBuilder column(String columnName, String type){
        this.columns.add(columnName + " " + type);
        return this;
    }

    @Override
    public String toString(){
        StringBuilder sql = new StringBuilder("CREATE TABLE ");
        columns.add("id INT NOT NULL AUTO_INCREMENT PRIMARY KEY");
        addStatementToQuery(sql, tables, "", ", ");
        addStatementToQuery(sql, columns, "(", ", ");
        sql.append(");");

        tables.clear();
        columns.clear();

        return sql.toString();
    }
}
