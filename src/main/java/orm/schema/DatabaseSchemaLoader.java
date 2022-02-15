package orm.schema;

import orm.session.Executor;
import orm.sql.CommandType;
import orm.sql.QueryBuilder;
import orm.utils.Config;

import javax.sql.rowset.CachedRowSet;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class DatabaseSchemaLoader {
    public static Schema loadDatabaseSchema(Executor executor) throws Exception {
        QueryBuilder qb = new QueryBuilder(CommandType.SELECT);
        qb.addTable("information_schema.columns")
                .addColumn("DISTINCT TABLE_NAME")
                .addCondition("table_schema = '" + Config.getInstance().getDatabaseName() + "'");

        CachedRowSet crs = executor.execute(qb.build()).orElseThrow(Exception::new);

        Schema schema = new Schema();
        schema.setIdTableExists(false);

        List<String> tables = new ArrayList<>();
        while (crs.next()){
            var tableName = crs.getString("TABLE_NAME");
            if (tableName.equals("id")){
                schema.setIdTableExists(true);
            } else {
                tables.add(tableName);
            }
        }
        for (String table: tables){
            addTableToSchema(schema, executor, table);
        }

        return schema;
    }

    private static void addTableToSchema(Schema schema, Executor executor, String tableName) throws Exception {
        var qb = new QueryBuilder(CommandType.SELECT);
        qb.addTable("information_schema.columns COL "
                        + "LEFT JOIN INFORMATION_SCHEMA.KEY_COLUMN_USAGE KCU "
                        + "ON COL.COLUMN_NAME = KCU.COLUMN_NAME "
                        + "AND COL.TABLE_NAME = KCU.TABLE_NAME")
                .addColumn("COL.COLUMN_NAME")
                .addColumn("COL.COLUMN_TYPE")
                .addColumn("COL.COLUMN_KEY")
                .addColumn("KCU.REFERENCED_TABLE_NAME")
                .addCondition("COL.table_schema = '" + Config.getInstance().getDatabaseName() + "'")
                .addCondition("COL.TABLE_NAME = '" + tableName + "'");

        CachedRowSet crs = executor.execute(qb.build()).orElseThrow(Exception::new);
        Set<SimpleColumn> columns = new HashSet<>();
        while (crs.next()) {
            var columnName = crs.getString("COLUMN_NAME");
            var columnType = crs.getString("COLUMN_TYPE");
            var columnKey = crs.getString("COLUMN_KEY");
            var refTable = crs.getString("REFERENCED_TABLE_NAME");
            SimpleColumn column;
            if (columnName.equals("id"))
                continue;
            else if (columnKey.equals("MUL")){
                column = new ForeignKeyColumn(columnName, refTable);
            } else {
                column = new SimpleColumn(columnName, columnType);
            }
            columns.add(column);
        }
            schema.getTables().put(tableName, columns);
    }

}
