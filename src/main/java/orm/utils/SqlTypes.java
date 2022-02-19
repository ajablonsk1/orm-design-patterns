package orm.utils;

import java.util.Date;

public class SqlTypes {
    public static String getType(Class<?> cl) {
        if (cl == String.class || cl == Character.class || cl == char.class) {
            return "varchar(50)";
        }
        if (cl == boolean.class || cl == Boolean.class) {
            return "boolean";
        }
        if (cl == byte.class || cl == Byte.class) {
            return "tinyint";
        }
        if (cl == short.class || cl == Short.class) {
            return "smallint";
        }
        if (cl == int.class || cl == Integer.class) {
            return "int";
        }
        if (cl == long.class || cl == Long.class) {
            return "bigint";
        }
        if (cl == float.class || cl == Float.class) {
            return "float";
        }
        if (cl == double.class || cl == Double.class) {
            return "double";
        }
        if (cl == byte[].class) {
            return "blob";
        }
        if (cl == Date.class) {
            return "datetime";
        }
        return "Incorrect type";
    }
}
