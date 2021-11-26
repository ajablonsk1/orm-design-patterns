import org.junit.jupiter.api.Test;
import orm.sql.CreateTableSqlBuilder;
import orm.sql.DropTableSqlBuilder;
import orm.sql.InsertRecordSqlBuilder;
import orm.sql.SelectRecordsSqlBuilder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class BuilderTests {

    @Test
    public void createTableSqlBuilder(){
        CreateTableSqlBuilder createTableSqlBuilder = new CreateTableSqlBuilder();
        createTableSqlBuilder.table("table1")
                .column("column1", "int")
                .column("column2", "varchar(50)");
        String s1 = createTableSqlBuilder.toString();
        String s2 = "CREATE TABLE table1(column1 int, column2 varchar(50));";
        assertEquals(s1, s2);
    }

    @Test
    public void dropTableSqlBuilder(){
        DropTableSqlBuilder dropTableSqlBuilder = new DropTableSqlBuilder();
        dropTableSqlBuilder.table("table1").table("table2");
        String s1 = dropTableSqlBuilder.toString();
        String s2 = "DROP TABLE table1, table2;";
        assertEquals(s1, s2);
    }

    @Test
    public void insertRecordSqlBuilder(){
        InsertRecordSqlBuilder insertRecordSqlBuilder1 = new InsertRecordSqlBuilder();
        insertRecordSqlBuilder1.table("table1")
                .values("10, 'Tom'");
        String s1 = insertRecordSqlBuilder1.toString();
        String s2 = "INSERT INTO table1 VALUES(10, 'Tom');";
        assertEquals(s1, s2);
    }

    @Test
    public void selectTableSqlBuilder(){

        // Chwilowy test
        SelectRecordsSqlBuilder selectRecordsSqlBuilder = new SelectRecordsSqlBuilder();
        selectRecordsSqlBuilder.column("column1").min("column1");
        String s1 = selectRecordsSqlBuilder.toString();
        String s2 = "SELECT column1, MIN(column1) ";
        assertEquals(s1, s2);
    }
}
