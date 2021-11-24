package orm.scanner;

import orm.annotations.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Set;

public class ClassScanner {

    public void scanClasses(Set<Class> classes) {
        for (Class cl : classes) {
            scanClass(cl);
        }
    }

    private void scanClass(Class cl) {
        Annotation[] classAnnotations = cl.getAnnotations();
        for (Annotation annotation : classAnnotations) {
            if (annotation.annotationType() == Entity.class) {
                // TODO: wywołanie buildera
                // builder.createTable(cl)
            }
            // TODO: pozostałe adnotacje, jeżeli będą
        }

        Field[] fields = cl.getDeclaredFields();
        for (Field field : fields) {
            Annotation[] fieldAnnotations = field.getAnnotations();
            for (Annotation annotation : fieldAnnotations) {
                Class annotationType = annotation.annotationType();
                // TODO: tworzenie relacji między klasami
                if (annotationType == Column.class) {

                } else if (annotationType == Id.class) {

                } else if (annotationType == ManyToMany.class) {

                } else if (annotationType == ManyToOne.class) {

                } else if (annotationType == OneToMany.class) {

                } else if (annotationType == OneToOne.class) {
                    // buidler.addOneToOne(cl, field.getType());
                }
            }
        }

    }
}
