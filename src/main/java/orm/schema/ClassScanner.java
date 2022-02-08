package orm.schema;

import orm.annotations.*;

import java.lang.reflect.Field;
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

    public List<Field> getAnnotatedFields(Class cl) {
        return Arrays.stream(cl.getDeclaredFields())
                .filter(this::hasAnnotation)
                .toList();
    }

    public List<Field> getFieldsAnnotatedAs(Class cl, Class annotation){
        return Arrays.stream(cl.getDeclaredFields())
                .filter(f -> hasSpecificAnnotation(f, annotation))
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
    public Class getParentEntityClass(Class cl){
        Class superclass = cl.getSuperclass();
        if (isEntity(superclass))
            return superclass;
        else return null;
    }

    private boolean hasSpecificAnnotation(Field field, Class annotation){
        return Arrays.stream(field.getAnnotations())
                .anyMatch(a -> a.annotationType() == annotation);
    }

    private boolean hasAnnotation(Field field){
        return !Arrays.asList(field.getAnnotations()).isEmpty();
    }

    private boolean isEntity(Class cl){
        return Arrays.stream(cl.getAnnotations())
                .anyMatch(a -> a.annotationType() == Entity.class);
    }
}
