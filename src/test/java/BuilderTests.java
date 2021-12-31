import org.junit.jupiter.api.Test;
import orm.sql.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class BuilderTests {

    @Test
    public void createTableSqlBuilder(){
        QueryBuilder builder = new QueryBuilder();
        Query query = builder.setCommandType(CommandType.CREATE)
                .addTable("table1")
                .addColumn("column1", "int")
                .addColumn("column2", "varchar(50)")
                .build();
        String s1 = query.toString();
        String s2 = "CREATE TABLE table1 (column1 int, column2 varchar(50), id INT NOT NULL AUTO_INCREMENT PRIMARY KEY);";
        assertEquals(s2, s1);
    }

    @Test
    public void dropTableSqlBuilder(){
        QueryBuilder builder = new QueryBuilder();
        Query query = builder.setCommandType(CommandType.DROP)
                .addTable("table1")
                .addTable("table2")
                .build();
        String s1 = query.toString();
        String s2 = "DROP TABLE table1, table2;";
        assertEquals(s2, s1);
    }

    @Test
    public void insertRecordSqlBuilder(){
        QueryBuilder builder = new QueryBuilder();
        Query query = builder.setCommandType(CommandType.INSERT)
                .addTable("table1")
                .addValue("10")
                .addValue("Tom")
                .build();
        String s1 = query.toString();
        String s2 = "INSERT INTO table1 VALUES (?, ?);";
        assertEquals(s2, s1);
    }

    @Test
    public void selectTableSqlBuilder(){
        QueryBuilder builder = new QueryBuilder();
        Query query = builder.setCommandType(CommandType.SELECT)
                .addColumn("column1", "")
                .addColumn("column2", "")
                .groupByColumn("column1")
                .addAggregate(AggregateFunction.MIN, "column1")
                .addTable("table1")
                .addCondition("column1 > 30")
                .addCondition("column2 = 'something'")
                .build();
        String s1 = query.toString();
        String s2 = "SELECT column1, column2, MIN(column1) FROM table1 WHERE column1 > 30 AND column2 = 'something' GROUP BY column1;";
        assertEquals(s2, s1);

        builder = new QueryBuilder();
        query = builder.setCommandType(CommandType.SELECT)
                .distinct(true)
                .addColumn("column1", "")
                .addColumn("column2", "")
                .groupByColumn("column1")
                .addAggregate(AggregateFunction.COUNT, "column3")
                .addTable("table1")
                .addCondition("column2 > 2")
                .build();
        String s3 = query.toString();
        String s4 = "SELECT DISTINCT column1, column2, COUNT(column3) FROM table1 " +
                "WHERE column2 > 2 GROUP BY column1;";
        assertEquals(s4, s3);
    }

    @Test
    public void updateRecordsSqlBuilder(){
        QueryBuilder builder = new QueryBuilder();
        Query query = builder.setCommandType(CommandType.UPDATE)
                .addTable("table1")
                .setColumn("column1", "'Adrian'")
                .setColumn("column2", "30")
                .addCondition("customerID = 1")
                .build();
        String s1 = query.toString();
        String s2 = "UPDATE table1 SET column1 = ?, column2 = ? WHERE customerID = 1;";
        assertEquals(s2, s1);
    }

    @Test
    public void deleteRecordsSqlBuilder(){
        QueryBuilder builder = new QueryBuilder();
        Query query = builder.setCommandType(CommandType.DELETE)
                .addTable("table1")
                .addCondition("tableID = 1")
                .addCondition("column2 = 'something'")
                .build();
        String s1 = query.toString();
        String s2 = "DELETE FROM table1 WHERE tableID = 1 AND column2 = 'something';";
        assertEquals(s2, s1);
    }
}
