package orm.sql;

import java.util.LinkedList;
import java.util.List;

public class SelectRecordsSqlBuilder extends SqlBuilder{

    private List<String> columns = new LinkedList<>();

    private List<String> tables = new LinkedList<>();

    private List<String> wheres = new LinkedList<>();

    private List<String> groupBys = new LinkedList<>();

    private List<String> orderBys = new LinkedList<>();

    private List<String> joins = new LinkedList<>();

    private List<String> leftJoins = new LinkedList<>();

    private List<String> havings = new LinkedList<String>();

    private boolean distinct = false;

    private int limit = 0;

    private int offset = 0;

    public SelectRecordsSqlBuilder(){};

    public SelectRecordsSqlBuilder column(String columnName){
        this.columns.add(columnName);
        return this;
    }

    public SelectRecordsSqlBuilder table(String tableName){
        this.tables.add(tableName);
        return this;
    }

    public SelectRecordsSqlBuilder where(String condition){
        this.wheres.add(condition);
        return this;
    }

    public SelectRecordsSqlBuilder groupBy(String columnName){
        this.groupBys.add(columnName);
        return this;
    }

    public SelectRecordsSqlBuilder count(String columnName){
        this.columns.add("COUNT(" + columnName + ") ");
        return this;
    }

    public SelectRecordsSqlBuilder avg(String columnName){
        this.columns.add("AVG(" + columnName + ") ");
        return this;
    }

    public SelectRecordsSqlBuilder sum(String columnName){
        this.columns.add("SUM(" + columnName + ") ");
        return this;
    }

    public SelectRecordsSqlBuilder join(String tableName){
        this.joins.add(tableName);
        return this;
    }

    public SelectRecordsSqlBuilder leftJoin(String tableName){
        this.leftJoins.add(tableName);
        return this;
    }

    public SelectRecordsSqlBuilder having(String condition){
        this.havings.add(condition);
        return this;
    }

    public SelectRecordsSqlBuilder andForWhere(String condition){
        this.wheres.add(condition);
        return this;
    }

    public SelectRecordsSqlBuilder orderBy(String columnName){
        this.orderBys.add(columnName);
        return this;
    }

    public SelectRecordsSqlBuilder andForHaving(String condition){
        this.havings.add(condition);
        return this;
    }

    public SelectRecordsSqlBuilder min(String columnName){
        this.columns.add("MIN(" + columnName + ") ");
        return this;
    }

    public SelectRecordsSqlBuilder max(String columnName){
        this.columns.add("MAX(" + columnName + ") ");
        return this;
    }

    public SelectRecordsSqlBuilder distinct(){
        this.distinct = true;
        return this;
    }

    @Override
    public String toString(){
        StringBuilder sql = new StringBuilder("SELECT ");
        if(distinct){
            sql.append("DISTINCT ");
        }
        if(columns.size() == 0){
            sql.append("(*) ");
        }
        else{
            addStatementToQuery(sql, columns, "", ", ");
        }

        addStatementToQuery(sql, tables, "FROM ", ", ")
                .addStatementToQuery(sql, joins, " JOIN ", " JOIN ")
                .addStatementToQuery(sql, leftJoins, " JOIN LEFT ", " JOIN LEFT ")
                .addStatementToQuery(sql, wheres, " WHERE ", " AND ")
                .addStatementToQuery(sql, groupBys, " GROUP BY ", ", " )
                .addStatementToQuery(sql, havings, " HAVING ", " AND ")
                .addStatementToQuery(sql, orderBys, " ORDER BY ", ", ");

        if(limit > 0)
            sql.append(" limit ")
                    .append(limit);
        if(offset > 0)
            sql.append(", ")
                    .append(offset);

        sql.append(";");

        columns.clear();
        tables.clear();
        joins.clear();
        leftJoins.clear();
        wheres.clear();
        columns.clear();
        groupBys.clear();
        havings.clear();
        orderBys.clear();

        return sql.toString();
    }
}
