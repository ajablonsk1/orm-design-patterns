import org.junit.jupiter.api.Test;
import orm.sql.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class BuilderTests {

    @Test
    public void createTableSqlBuilder(){
        CreateTableSqlBuilder createTableSqlBuilder = new CreateTableSqlBuilder();
        String s1 = createTableSqlBuilder.table("table1")
                .column("column1", "int")
                .column("column2", "varchar(50)")
                .toString();
        String s2 = "CREATE TABLE table1(column1 int, column2 varchar(50));";
        assertEquals(s1, s2);
    }

    @Test
    public void dropTableSqlBuilder(){
        DropTableSqlBuilder dropTableSqlBuilder = new DropTableSqlBuilder();
        String s1 = dropTableSqlBuilder.table("table1")
                .table("table2")
                .toString();
        String s2 = "DROP TABLE table1, table2;";
        assertEquals(s1, s2);
    }

    @Test
    public void insertRecordSqlBuilder(){
        InsertRecordsSqlBuilder insertRecordSqlBuilder1 = new InsertRecordsSqlBuilder();
        String s1 = insertRecordSqlBuilder1.table("table1")
                .values("10, 'Tom'")
                .toString();
        String s2 = "INSERT INTO table1 VALUES(10, 'Tom');";
        assertEquals(s1, s2);
    }

    @Test
    public void selectTableSqlBuilder(){
        SelectRecordsSqlBuilder selectRecordsSqlBuilder = new SelectRecordsSqlBuilder();
        String s1 = selectRecordsSqlBuilder.column("column1")
                .column("column2")
                .min("column1")
                .table("table1")
                .where("column1 > 30")
                .andForWhere("column2 = 'something'")
                .toString();
        String s2 = "SELECT column1, column2, MIN(column1) FROM table1 WHERE column1 > 30 AND column2 = 'something';";
        assertEquals(s1, s2);

        String s3 = selectRecordsSqlBuilder.distinct()
                .column("column1")
                .column("column2")
                .count("column3")
                .table("table1")
                .where("column2 > 2")
                .groupBy("column1")
                .having("column3 < 30")
                .orderBy("column3")
                .toString();
        String s4 = "SELECT DISTINCT column1, column2, COUNT(column3) FROM table1 " +
                "WHERE column2 > 2 GROUP BY column1 HAVING column3 < 30 ORDER BY column3;";
        assertEquals(s3, s4);

        String s5 = selectRecordsSqlBuilder.distinct()
                .table("table1")
                .having("column1 > 30")
                .andForHaving("column2 < 15")
                .toString();
        String s6 = "SELECT DISTINCT (*) FROM table1 HAVING column1 > 30 AND column2 < 15;";
        assertEquals(s5, s6);
    }

    @Test
    public void updateRecordsSqlBuilder(){
        UpdateRecordsSqlBuilder updateRecordsSqlBuilder = new UpdateRecordsSqlBuilder();
        String s1 = updateRecordsSqlBuilder.table("table1")
                .set("column1", "'Adrian'")
                .set("column2", "30")
                .where("customerID = 1")
                .toString();
        String s2 = "UPDATE table1 SET column1 = 'Adrian', column2 = 30 WHERE customerID = 1;";
        assertEquals(s1, s2);
    }

    @Test
    public void deleteRecordsSqlBuilder(){
        DeleteRecordsSqlBuilder deleteRecordsSqlBuilder = new DeleteRecordsSqlBuilder();
        String s1 = deleteRecordsSqlBuilder.table("table1")
                .where("tableID = 1")
                .and("column2 = 'something'")
                .toString();
        String s2 = "DELETE FROM table1 WHERE tableID = 1 AND column2 = 'something';";
        assertEquals(s1, s2);
    }
}
