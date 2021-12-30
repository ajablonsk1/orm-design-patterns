package orm.sql;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Query {
    CommandType commandType = null;
    boolean isGrouped = false;
    boolean distinct = false;
    List<String> tables = new ArrayList<>();
    List<String> columns = new ArrayList<>();
    List<String> conditions = new ArrayList<>();
    List<Object> values = new ArrayList<>();
    List<String> groupByColumns = new ArrayList<>();
    List<Map.Entry<AggregateFunction, String>> aggregateFunctions = new ArrayList<>();
    List<String> setColumns = new ArrayList<>();

    public String toString() {
        if (commandType == null) {
            throw new IllegalStateException("Command type not set");
        }
        StringBuilder sql = new StringBuilder();
        if (commandType == CommandType.CREATE) {
            sql.append(commandType.toString() + " ");
            sql.append(String.join(", ", tables));
            columns.add("id INT NOT NULL AUTO_INCREMENT PRIMARY KEY");
            sql.append(" (");
            sql.append(String.join(", ", columns));
            sql.append(");");
        }
        if (commandType == CommandType.DELETE) {
            sql.append(commandType.toString() + " ");
            sql.append(String.join(", ", tables));
            sql.append(" WHERE ");
            sql.append(String.join(" AND ", conditions));
            sql.append(";");
        }
        if (commandType == CommandType.DROP) {
            sql.append(commandType.toString() + " ");
            sql.append(String.join(", ", tables));
            sql.append(";");
        }
        if (commandType == CommandType.INSERT) {
            sql.append(commandType.toString() + " ");
            sql.append(String.join(", ", tables));
            if (columns.size() != 0) {
                sql.append(" (");
                sql.append(String.join(", ", columns));
                sql.append(")");
            }
            sql.append(" VALUES (");
            sql.append("?");
            values.remove(0);
            values.forEach((value) -> sql.append(", ?"));
            sql.append(");");
        }
        if (commandType == CommandType.SELECT) {
            sql.append(commandType.toString() + " ");
            if (distinct)
                sql.append("DISTINCT ");
            if (aggregateFunctions.size() == 0 && columns.size() == 0)
                sql.append("(*) ");
            sql.append(String.join(", ", columns));
            if (aggregateFunctions.size() != 0) {
                Map.Entry<AggregateFunction, String> entry = aggregateFunctions.remove(0);
                sql.append(", " + entry.getKey().toString(entry.getValue()));
                aggregateFunctions.forEach((func) -> {
                    sql.append(", " + func.getKey().toString(func.getValue()));
                });
            }
            sql.append(" FROM ");
            sql.append(String.join(", ", tables));
            if (conditions.size() != 0) {
                sql.append(" WHERE ");
                sql.append(String.join(" AND ", conditions));
            }
            if (isGrouped) {
                sql.append(" GROUP BY ");
                sql.append(String.join(", ", groupByColumns));
            }
            sql.append(";");
        }
        if (commandType == CommandType.UPDATE) {
            sql.append(commandType.toString() + " ");
            sql.append(String.join(", ", tables));
            sql.append(" SET ");
            sql.append(String.join(", ", setColumns));
            if (conditions.size() != 0) {
                sql.append(" WHERE ");
                sql.append(String.join(" AND ", conditions));
            }
            sql.append(";");
        }
        if (commandType == CommandType.ALTER) {
            // TODO: Inne rodzaje polecenia ALTER TABLE
            sql.append(commandType.toString() + " ");
            sql.append(String.join(", ", tables));
            sql.append(" ADD ");
            sql.append(String.join(", ", columns));
            sql.append(";");
        }
        return sql.toString();
    }

    public List<Object> getValues() {
        return values;
    }
}
