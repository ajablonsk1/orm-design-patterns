package orm.utils;

import orm.schema.reflect.ClassScanner;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;

public class IdService {
    ClassScanner classScanner = new ClassScanner();

    public void setObjectId(Object object, int id) throws IllegalAccessException {
        Field idField = classScanner.getIdField(object.getClass());
        Field[] arr = new Field[1];
        arr[0] = idField;
        AccessibleObject.setAccessible(arr, true);
        idField.set(object, id);
    }

    public Integer getObjectId(Object object) throws IllegalAccessException {
        if (object == null){
            return null;
        }
        Field idField = classScanner.getIdField(object.getClass());
        Field[] arr = new Field[1];
        arr[0] = idField;
        AccessibleObject.setAccessible(arr, true);
        return (int) idField.get(object);
    }
}
