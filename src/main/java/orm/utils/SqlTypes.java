package orm.utils;

import java.util.Date;

public class SqlTypes {
    public static String getType(Class<?> cl) {
        if (cl == String.class || cl == Character.class || cl == char.class) {
            return "varchar(50)";
        }
        if (cl == boolean.class || cl == Boolean.class) {
            return "BOOLEAN";
        }
        if (cl == byte.class || cl == Byte.class) {
            return "TINYINT";
        }
        if (cl == short.class || cl == Short.class) {
            return "SMALLINT";
        }
        if (cl == int.class || cl == Integer.class) {
            return "INT";
        }
        if (cl == long.class || cl == Long.class) {
            return "BIGINT";
        }
        if (cl == float.class || cl == Float.class) {
            return "FLOAT";
        }
        if (cl == double.class || cl == Double.class) {
            return "DOUBLE";
        }
        if (cl == byte[].class) {
            return "BLOB";
        }
        if (cl == Date.class) {
            return "DATETIME";
        }
        return "Incorrect type";
    }
}
