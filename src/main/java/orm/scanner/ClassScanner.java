package orm.scanner;

import orm.annotations.*;
import orm.executor.SqlExecutor;
import orm.sql.CommandType;
import orm.sql.Query;
import orm.sql.QueryBuilder;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

public class ClassScanner {

    private final SqlExecutor executor;

    public ClassScanner(SqlExecutor executor) {
        this.executor = executor;
    }

    public void scanClasses(Set<Class> classes) {
        for (Class currentClass : classes) {
            scanClass(classes, currentClass);
        }
    }

    // TODO: Przeniesienie tworzenia tabel do osobnej klasy
    private void scanClass(Set<Class> classes, Class currentClass) {
        Annotation[] classAnnotations = currentClass.getAnnotations();
        for (Annotation annotation : classAnnotations) {
            if (annotation.annotationType() == Entity.class) {
                QueryBuilder queryBuilder = new QueryBuilder();
                Query query = queryBuilder.setCommandType(CommandType.CREATE)
                        .addTable(currentClass)
                        .build();
                executor.execute(query);
            }
            // TODO: pozostałe adnotacje, jeżeli będą
        }

        Field[] fields = currentClass.getDeclaredFields();
        for (Field field : fields) {
            Annotation[] fieldAnnotations = field.getAnnotations();
            for (Annotation annotation : fieldAnnotations) {
                Class annotationType = annotation.annotationType();
                // TODO: tworzenie relacji między klasami
                if (annotationType == Column.class) {
                    // TODO: Dodanie kolumn z klas nadrzędnych
                    QueryBuilder queryBuilder = new QueryBuilder();
                    Query query = queryBuilder.setCommandType(CommandType.ALTER)
                            .addTable(currentClass)
                            .addColumn(field)
                            .build();
                    executor.execute(query);
                } else if (annotationType == Id.class) {

                } else if (annotationType == ManyToMany.class) {

                } else if (annotationType == ManyToOne.class) {
//                    if (!classes.contains(getElementClass(field))) {
//                        // kolekcja nie zawiera typu oznaczonego adnotacją @Entity
//                    }
                } else if (annotationType == OneToMany.class) {

                } else if (annotationType == OneToOne.class) {
//                    if (!classes.contains(field.getType())) {
//                        // brak @Entity przy klasie, z którą ma być relacja - błąd
//                    }
                    // buidler.addOneToOne(cl, field.getType());
                }
            }
        }

    }
    private Class getElementClass(Field field) {
        Type type = field.getGenericType();
        Class ret;
        if (type instanceof ParameterizedType) {
            Type[] typeArguments = ((ParameterizedType) type).getActualTypeArguments();
            if (typeArguments.length == 0)
                throw new IllegalArgumentException("Field not parametrised by any type");
            if (typeArguments.length != 1) {
                throw new IllegalArgumentException("Field parametrised by more than one type");
            }
            if (!(typeArguments[0] instanceof Class)) {
                throw new IllegalArgumentException("Field not parametrised by Class");
            }
            ret = (Class) typeArguments[0];
        } else {
            throw new IllegalArgumentException("Field not parametrised by any type");
        }
        return ret;
    }

    /**
     * Returns list of {@code Field}, {@code Object} pairs, representing
     * values of argument fields annotated with {@code @Column} annotation.
     * Fields are searched for whole class hierarchy till class not annotated
     * with {@code @Entity} annotation is found.
     * @param object object, of which fields are to be returned by this method
     * @return       map of {@code Field}, {@code Object} pairs
     */
    public List<Map.Entry<Field, Object>> getColumnValues(Object object) {
        List<Map.Entry<Field, Object>> result = new ArrayList<>();
        List<Field> columns = getColumns(object.getClass());
        for (Field field : columns) {
            field.setAccessible(true);
            try {
                result.add(new AbstractMap.SimpleEntry<>(field, field.get(object)));
            } catch (IllegalAccessException e) {
                // TODO: obsługa błędu
                e.printStackTrace();
            }
            field.setAccessible(false);
        }
        return result;
    }

    public List<Field> getColumns(Class cl) {
        List<Field> result = new ArrayList<>();
        while (cl.isAnnotationPresent(Entity.class)) {
            Field[] fields = cl.getDeclaredFields();
            for (Field field : fields) {
                if (field.isAnnotationPresent(Column.class)) {
                    result.add(field);
                }
            }
            cl = cl.getSuperclass();
        }
        return result;
    }
}
