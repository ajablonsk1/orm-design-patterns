package orm.session.operations;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.Locale;

public class AssociationTableService {
    public static String getColumnNameForField(Field field){
        return ((Class<?>) ((ParameterizedType) field.getGenericType())
                .getActualTypeArguments()[0])
                .getSimpleName()
                .toLowerCase() + "_id";
    }
    public static String getColumnNameForClass(Class<?> cl){
        return cl.getSimpleName().toLowerCase(Locale.ROOT) + "_id";
    }

}
