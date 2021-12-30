package orm.sql;

import java.lang.reflect.Field;
import java.util.*;

public class QueryBuilder {
    private Query query = new Query();

    public QueryBuilder setCommandType(CommandType commandType) {
        query.setCommandType(commandType);
        return this;
    }

    public QueryBuilder addTable(Class cl) {
        if (query.getCommandType() == null) {
            throw new IllegalStateException("Command type not set");
        }
        query.getTables().add(cl.getSimpleName().toLowerCase());
        return this;
    }

    public QueryBuilder addTable(String table) {
        if (query.getCommandType() == null) {
            throw new IllegalStateException("Command type not set");
        }
        query.getTables().add(table);
        return this;
    }

    public QueryBuilder addColumn(Field field) {
        if (query.getCommandType() == null) {
            throw new IllegalStateException("Command type not set");
        }
        if (query.getCommandType() == CommandType.CREATE || query.getCommandType() == CommandType.ALTER) {
            String type = getSqlTypeFromClass(field.getType());
            query.getColumns().add(field.getName().toLowerCase() + " " + type);
        }
        if (query.getCommandType() == CommandType.SELECT || query.getCommandType() == CommandType.INSERT) {
            query.getColumns().add(field.getName().toLowerCase());
        }
        return this;
    }

    public QueryBuilder addColumn(String column, String type) {
        if (query.getCommandType() == null) {
            throw new IllegalStateException("Command type not set");
        }
        if (query.getCommandType() == CommandType.CREATE || query.getCommandType() == CommandType.ALTER) {
            query.getColumns().add(column + " " + type);
        }
        if (query.getCommandType() == CommandType.SELECT) {
            query.getColumns().add(column);
        }
        return this;
    }

    public QueryBuilder addCondition(String condition) {
        if (query.getCommandType() == null) {
            throw new IllegalStateException("Command type not set");
        }
        query.getConditions().add(condition);
        return this;
    }

    public QueryBuilder addValue(Object value) {
        if (query.getCommandType() == null) {
            throw new IllegalStateException("Command type not set");
        }
        query.getValues().add(value);
        return this;
    }

    public QueryBuilder distinct(boolean distinct) {
        if (query.getCommandType() == null) {
            throw new IllegalStateException("Command type not set");
        }
        if (query.getCommandType() != CommandType.SELECT) {
            throw new IllegalStateException("Command is not set to SELECT");
        }
        query.setDistinct(distinct);
        return this;
    }

    public QueryBuilder groupByColumn(String column) {
        if (query.getCommandType() == null) {
            throw new IllegalStateException("Command type not set");
        }
        if (query.getCommandType() != CommandType.SELECT) {
            throw new IllegalStateException("Command is not set to SELECT");
        }
        query.getGroupByColumns().add(column);
        query.setGrouped(true);
        return this;
    }

    public QueryBuilder groupByColumn(Field field) {
        if (query.getCommandType() == null) {
            throw new IllegalStateException("Command type not set");
        }
        if (query.getCommandType() != CommandType.SELECT) {
            throw new IllegalStateException("Command is not set to SELECT");
        }
        query.getGroupByColumns().add(field.getName().toLowerCase());
        query.setGrouped(true);
        return this;
    }

    public QueryBuilder addAggregate(AggregateFunction function, String column) {
        if (query.getCommandType() == null) {
            throw new IllegalStateException("Command type not set");
        }
        if (query.getCommandType() != CommandType.SELECT) {
            throw new IllegalStateException("Command is not set to SELECT");
        }
        if (!query.isGrouped()) {
            throw new IllegalStateException("No GROUP BY statement");
        }
        query.getAggregateFunctions().add(new AbstractMap.SimpleEntry<AggregateFunction, String>(function, column));
        return this;
    }

    public QueryBuilder setColumn(String column, Object value) {
        if (query.getCommandType() == null) {
            throw new IllegalStateException("Command type not set");
        }
        if (query.getCommandType() != CommandType.UPDATE) {
            throw new IllegalStateException("Command is not set to UPDATE");
        }
        query.getSetColumns().add(column + " = ?");
        query.getValues().add(value);
        return this;
    }

    public QueryBuilder setColumn(Field column, Object value) {
        if (query.getCommandType() == null) {
            throw new IllegalStateException("Command type not set");
        }
        if (query.getCommandType() != CommandType.UPDATE) {
            throw new IllegalStateException("Command is not set to UPDATE");
        }
        query.getSetColumns().add(column.getName().toLowerCase() + "= ?");
        query.getValues().add(value);
        return this;
    }

    public Query build() {
        return query;
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
