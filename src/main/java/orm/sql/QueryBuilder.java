package orm.sql;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class QueryBuilder {
    private CommandType commandType = null;
    private List<String> tables = new ArrayList<>();
    private List<String> columns = new ArrayList<>();
    private List<String> conditions = new ArrayList<>();
    private List<String> values = new ArrayList<>();
    private List<String> groupByColumns = new ArrayList<>();
    private List<String> aggregateFunctions = new ArrayList<>();
    private List<String> setColumns = new ArrayList<>();
    private boolean isGrouped = false;
    private boolean distinct = false;

    public QueryBuilder setCommandType(CommandType commandType) {
        this.commandType = commandType;
        return this;
    }

    public QueryBuilder addTable(Class cl) {
        if (commandType == null) {
            throw new IllegalStateException("Command type not set");
        }
        tables.add(cl.getSimpleName().toLowerCase());
        return this;
    }

    public QueryBuilder addTable(String table) {
        if (commandType == null) {
            throw new IllegalStateException("Command type not set");
        }
        tables.add(table);
        return this;
    }

    public QueryBuilder addColumn(Field field) {
        if (commandType == null) {
            throw new IllegalStateException("Command type not set");
        }
        if (commandType == CommandType.CREATE || commandType == CommandType.ALTER) {
            String type = getSqlTypeFromClass(field.getType());
            columns.add(field.getName().toLowerCase() + " " + type);
        }
        if (commandType == CommandType.SELECT || commandType == CommandType.INSERT) {
            columns.add(field.getName().toLowerCase());
        }
        return this;
    }

    public QueryBuilder addColumn(String column, String type) {
        if (commandType == null) {
            throw new IllegalStateException("Command type not set");
        }
        if (commandType == CommandType.CREATE || commandType == CommandType.ALTER) {
            columns.add(column + " " + type);
        }
        if (commandType == CommandType.SELECT) {
            columns.add(column);
        }
        return this;
    }

    public QueryBuilder addCondition(String condition) {
        if (commandType == null) {
            throw new IllegalStateException("Command type not set");
        }
        conditions.add(condition);
        return this;
    }

    public QueryBuilder addValue(Object value) {
        if (commandType == null) {
            throw new IllegalStateException("Command type not set");
        }
        values.add(stringValue(value));
        return this;
    }

    public QueryBuilder distinct(boolean distinct) {
        if (commandType == null) {
            throw new IllegalStateException("Command type not set");
        }
        if (commandType != CommandType.SELECT) {
            throw new IllegalStateException("Command is not set to SELECT");
        }
        this.distinct = distinct;
        return this;
    }

    public QueryBuilder groupByColumn(String column) {
        if (commandType == null) {
            throw new IllegalStateException("Command type not set");
        }
        if (commandType != CommandType.SELECT) {
            throw new IllegalStateException("Command is not set to SELECT");
        }
        groupByColumns.add(column);
        return this;
    }

    public QueryBuilder groupByColumn(Field field) {
        if (commandType == null) {
            throw new IllegalStateException("Command type not set");
        }
        if (commandType != CommandType.SELECT) {
            throw new IllegalStateException("Command is not set to SELECT");
        }
        groupByColumns.add(field.getName().toLowerCase());
        isGrouped = true;
        return this;
    }

    public QueryBuilder setAggregate(AggregateFunction function, String column) {
        if (commandType == null) {
            throw new IllegalStateException("Command type not set");
        }
        if (commandType != CommandType.SELECT) {
            throw new IllegalStateException("Command is not set to SELECT");
        }
        if (!isGrouped) {
            throw new IllegalStateException("No GROUP BY statement");
        }
        function.setColumm(column);
        aggregateFunctions.add(function.toString());
        return this;
    }

    public QueryBuilder setColumn(String column, Object value) {
        if (commandType == null) {
            throw new IllegalStateException("Command type not set");
        }
        if (commandType != CommandType.UPDATE) {
            throw new IllegalStateException("Command is not set to UPDATE");
        }
        setColumns.add(column + "=" + stringValue(value));
        return this;
    }

    public QueryBuilder setColumn(Field column, Object value) {
        if (commandType == null) {
            throw new IllegalStateException("Command type not set");
        }
        if (commandType != CommandType.UPDATE) {
            throw new IllegalStateException("Command is not set to UPDATE");
        }
        setColumns.add(column.getName().toLowerCase() + "=" + stringValue(value));
        return this;
    }

    public QueryBuilder setColumn(Field column, String value) {
        if (commandType == null) {
            throw new IllegalStateException("Command type not set");
        }
        if (commandType != CommandType.UPDATE) {
            throw new IllegalStateException("Command is not set to UPDATE");
        }
        setColumns.add(column.getName().toLowerCase() + "=" + value);
        return this;
    }

    public QueryBuilder setColumn(String column, String value) {
        if (commandType == null) {
            throw new IllegalStateException("Command type not set");
        }
        if (commandType != CommandType.UPDATE) {
            throw new IllegalStateException("Command is not set to UPDATE");
        }
        setColumns.add(column + "=" + value);
        return this;
    }

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
            sql.append(" (");
            sql.append(String.join(", ", columns));
            sql.append(") VALUES ");
            sql.append(String.join(", ", values));
            sql.append(";");
        }
        if (commandType == CommandType.SELECT) {
            sql.append(commandType.toString() + " ");
            if (distinct)
                sql.append("DISTINCT ");
            if (aggregateFunctions.size() == 0 && columns.size() == 0)
                sql.append("(*) ");
            sql.append(String.join(", ", columns));
            if (aggregateFunctions.size() != 0) {
                sql.append(" ");
                sql.append(String.join(", ", aggregateFunctions));
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

    private String stringValue(Object value) {
        if (value.getClass() == Boolean.class) {
            return (Boolean) value ? String.valueOf(1) : String.valueOf(0);
        }
        return value.toString();
    }

    private String getSqlTypeFromClass(Class cl) {
        if (cl == String.class || cl == Character.class || cl == char.class) {
            return "VARCHAR(50)";
        }
        if (cl == boolean.class || cl == Boolean.class) {
            return "BOOLEAN";
        }
        if (cl == byte.class || cl == Byte.class) {
            return "TINYINT";
        }
        if (cl == short.class || cl == Short.class) {
            return "SMALLINT";
        }
        if (cl == int.class || cl == Integer.class) {
            return "INT";
        }
        if (cl == long.class || cl == Long.class) {
            return "BIGINT";
        }
        if (cl == float.class || cl == Float.class) {
            return "FLOAT";
        }
        if (cl == double.class || cl == Double.class) {
            return "DOUBLE";
        }
        if (cl == byte[].class) {
            return "BLOB";
        }
        if (cl == Date.class) {
            return "DATETIME";
        }
        return "Incorrect type";
    }
}
