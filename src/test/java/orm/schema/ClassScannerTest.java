package orm.schema;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import orm.annotations.*;
import orm.schema.reflect.ClassScanner;

import java.lang.reflect.Field;
import java.util.List;

public class ClassScannerTest {
    @Test
    public void getColumns(){
        //given
        class TestClass{
            @Column private String someColumnField;
            @Column @OneToOne(foreignKeyInThisTable = true) private String someOneToOneField;
            private String someNonColumnField;
        }
        ClassScanner scanner = new ClassScanner();
        //when
        List<Field> columns = scanner.getFieldsAnnotatedAs(TestClass.class, Column.class);
        //then
        assertTrue(columns.stream().allMatch(f -> f.isAnnotationPresent(Column.class)));
        assertTrue(columns.stream().anyMatch(f -> f.getName().equals("someColumnField")));
        assertTrue(columns.stream().anyMatch(f -> f.getName().equals("someOneToOneField")));
        assertTrue(columns.stream().noneMatch(f -> f.getName().equals("someNonColumnField")));
    }

    @Test
    public void getParentEntityClasses(){
        //given
        class BaseNotEntity{ }
        @Entity class BaseEntity extends BaseNotEntity{ }
        @Entity class SomeEntity extends BaseEntity{ }
        @Entity class ChildEntity extends SomeEntity{ }
        ClassScanner scanner = new ClassScanner();
        //when
        List<Class<?>> classes = scanner.getParentEntityClasses(ChildEntity.class);
        //then
        assertTrue(classes.stream().allMatch(c -> c.isAnnotationPresent(Entity.class)));
        assertTrue(classes.stream().anyMatch(c -> c.getSimpleName().equals("SomeEntity")));
        assertTrue(classes.stream().anyMatch(c -> c.getSimpleName().equals("BaseEntity")));
        assertTrue(classes.stream().noneMatch(c -> c.getSimpleName().equals("ChildEntity")));
        assertTrue(classes.stream().noneMatch(c -> c.getSimpleName().equals("BaseNotEntity")));
    }

    @Test
    public void hasAnnotation() throws NoSuchFieldException {
        //given
        class TestClass {
            @Column
            public String a;

            @OneToOne(foreignKeyInThisTable = true)
            public String b;

            @OneToMany
            public String c;

            @ManyToOne
            public String d;

            @ManyToMany(tableName = "tabela")
            public String e;

            @Id
            public String f;

            public String g;

            @Deprecated
            public String h;

        }
        ClassScanner scanner = new ClassScanner();

        assertTrue(scanner.hasAnnotation(TestClass.class.getDeclaredField("a")));
        assertTrue(scanner.hasAnnotation(TestClass.class.getDeclaredField("b")));
        assertTrue(scanner.hasAnnotation(TestClass.class.getDeclaredField("c")));
        assertTrue(scanner.hasAnnotation(TestClass.class.getDeclaredField("d")));
        assertTrue(scanner.hasAnnotation(TestClass.class.getDeclaredField("e")));
        assertTrue(scanner.hasAnnotation(TestClass.class.getDeclaredField("f")));
        assertFalse(scanner.hasAnnotation(TestClass.class.getDeclaredField("g")));
        assertFalse(scanner.hasAnnotation(TestClass.class.getDeclaredField("h")));
    }
}
