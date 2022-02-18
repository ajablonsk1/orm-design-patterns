package orm.utils;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public class Utils {
    public static Class<?> getInsideClass(Field field){
        return (Class<?>) (((ParameterizedType) field.getGenericType())
                .getActualTypeArguments()[0]);
    }
    public static String getInsideClassName(Field field){
        return getInsideClass(field).getSimpleName();
    }
    public static String getInsideClassNameLowercase(Field field){
        return getInsideClassName(field).toLowerCase();
    }
}
