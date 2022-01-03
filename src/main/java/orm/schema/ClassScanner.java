package orm.schema;

import orm.annotations.*;
import orm.session.Executor;
import orm.sql.CommandType;
import orm.sql.Query;
import orm.sql.QueryBuilder;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

public class ClassScanner {
    public List<Field> getColumns(Class cl){
        return getFieldsAnnotatedAs(cl, Column.class);
    }
    public List<Field> getOneToOneFields(Class cl){
        return getFieldsAnnotatedAs(cl, OneToOne.class);
    }
    public List<Field> getOneToManyFields(Class cl){
        return getFieldsAnnotatedAs(cl, OneToMany.class);
    }
    public List<Field> getManyToOneFields(Class cl){
        return getFieldsAnnotatedAs(cl, ManyToOne.class);
    }
    public List<Field> getManyToManyFields(Class cl){
        return getFieldsAnnotatedAs(cl, ManyToMany.class);
    }
    public List<Field> getFieldsAnnotatedAs(Class cl, Class annotation){
        return Arrays.stream(cl.getDeclaredFields())
                .filter(f -> hasAnnotation(f, annotation))
                .toList();
    }
    public List<Class> getParentEntityClasses(Class cl){
        Class superclass = cl.getSuperclass();
        if (isEntity(superclass)){
            List<Class> classes = getParentEntityClasses(superclass);
            classes.add(superclass);
            return classes;
        }
        return new ArrayList<>();
    }

    private boolean hasAnnotation(Field field, Class annotation){
        return Arrays.stream(field.getAnnotations())
                .anyMatch(a -> a.annotationType() == annotation);
    }

    private boolean isEntity(Class cl){
        return Arrays.stream(cl.getAnnotations())
                .anyMatch(a -> a.annotationType() == Entity.class);
    }
}
