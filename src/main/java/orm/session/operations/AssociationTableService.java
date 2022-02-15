package orm.session.operations;

import orm.utils.Utils;

import java.lang.reflect.Field;
import java.util.Locale;

public class AssociationTableService {
    public static String getColumnNameForField(Field field){
        return Utils.getInsideClassNameLowercase(field) + "_id";
    }
    public static String getColumnNameForClass(Class<?> cl){
        return cl.getSimpleName().toLowerCase(Locale.ROOT) + "_id";
    }

}
