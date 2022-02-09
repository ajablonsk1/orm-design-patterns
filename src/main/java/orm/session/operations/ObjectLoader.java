package orm.session.operations;

import orm.annotations.ManyToOne;
import orm.annotations.OneToMany;
import orm.annotations.OneToOne;
import orm.schema.ClassScanner;
import orm.session.Executor;
import orm.sql.CommandType;
import orm.sql.Query;
import orm.sql.QueryBuilder;

import javax.sql.rowset.CachedRowSet;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.sql.SQLException;
import java.util.*;

public class ObjectLoader {
    private Executor executor;
    private Map<Integer, Object> identityMap;
    private Map<Integer, Object> objectsBeingLoaded = new HashMap<>();
    private ClassScanner classScanner = new ClassScanner();

    public ObjectLoader(Executor executor, Map<Integer, Object> identityMap){
        this.executor = executor;
        this.identityMap = identityMap;
    }

    public Object load(Class<?> clazz, Integer id) throws NoSuchMethodException, SQLException, InvocationTargetException, InstantiationException, IllegalAccessException {
        //sprawdz czy juz byl wczytany
        if (! objectsBeingLoaded.containsKey(id) && identityMap.containsKey(id))
            return identityMap.get(id);

        Object instance;
        if (objectsBeingLoaded.containsKey(id)) // instancja już istnieje (utworzona na niższym poziomie)
            instance = objectsBeingLoaded.get(id);
        else { // jesteśmy na najniższym poziomie
            instance = clazz.getDeclaredConstructor().newInstance();
            objectsBeingLoaded.put(id, instance);
            identityMap.put(id, instance);
        }

        Class<?> parentEntityClass = classScanner.getParentEntityClass(clazz);
        if (parentEntityClass == null) // jeżeli jesteśmy na najwyższym poziomie dziedziczenia
            objectsBeingLoaded.remove(id);
        else
            instance = load(parentEntityClass, id);

        // wczytaj pola obiektu z bazy danych
        Query query = new QueryBuilder(CommandType.SELECT).addTable(clazz)
                .addCondition("id = " + id).build();
        CachedRowSet cachedRowSet = executor.execute(query)
                .orElseThrow(SQLException::new);
        cachedRowSet.next();

        // wez wszystkie pola z adnotacjami do ustawienia
        Field[] annotatedFields = classScanner.getAnnotatedFields(clazz).toArray(new Field[0]);
        AccessibleObject.setAccessible(annotatedFields, true);

        for (Field field: annotatedFields){
            Class<?> fieldType = field.getType();
            Object fieldValue;

            if (field.isAnnotationPresent(OneToOne.class)){
                fieldValue = getOneToOneFieldValue(clazz, id, cachedRowSet, field, fieldType);
            }
            else if (field.isAnnotationPresent(OneToMany.class)) {
                fieldValue = getOneToManyFieldValue(clazz, id, field);
            }
            else if (field.isAnnotationPresent(ManyToOne.class)) {
                Integer foreignKey = cachedRowSet.getInt(fieldType.getSimpleName() + "_id");
                fieldValue = load(fieldType, foreignKey);
            }
            else {
                //Only primitive
                fieldValue = cachedRowSet.getObject(field.getName().toLowerCase());
            }
            setField(instance, field, fieldValue);
        }
        return instance;
    }

    private Object getOneToManyFieldValue(Class<?> clazz, Integer id, Field field) throws SQLException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        Object fieldValue;
        //tu jest lista generic zwraca x List<x>
        String[] a = field.getGenericType().toString().split("[.>]");
        String entityNameToLoad = a[a.length-1].toLowerCase();

        // build query to get a list of items
        Query query1 = new QueryBuilder(CommandType.SELECT).addTable(entityNameToLoad)
                .addColumn("id","")
                .addCondition(clazz.getSimpleName().toLowerCase() + "_id = " + id)
                .build();

        CachedRowSet set = executor.execute(query1).orElseThrow(SQLException::new);

        //TODO: nie obslugujemy hashsetow jakby komus sie zachcialo
        Collection<Object> container = new ArrayList<>();
        while (set.next()){
            Integer ids = set.getInt("id");
            ParameterizedType pType = (ParameterizedType) field.getGenericType();
            Class<?> collectionType = (Class<?>) pType.getActualTypeArguments()[0];

            container.add(load(collectionType, ids));
        }
        fieldValue = container;
        return fieldValue;
    }

    private Object getOneToOneFieldValue(Class<?> clazz, Integer id, CachedRowSet cachedRowSet, Field field, Class<?> fieldType) {
        Object fieldValue = null;
        try {
            String[] a = fieldType.toString().split("\\.");
            String entityName = a[a.length - 1].toLowerCase();

            Integer otherId = field.getAnnotation(OneToOne.class).foreignKeyInThisTable()
                    ? cachedRowSet.getInt(entityName + "_id")
                    : findKey(entityName, id, clazz.toString().toLowerCase() + "_id");
            fieldValue = load(fieldType, otherId);
        } catch (Exception e){
            e.printStackTrace();
        }
        return fieldValue;
    }

    private boolean setField(Object object, Field field, Object fieldValue){

        try {
            field.set(object,fieldValue);
            return true;
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return false;
    }

    private Integer findKey(String tableName, Integer foreignKey, String column) throws SQLException {

        String[] array = column.split("\\.");
        column = array[array.length-1].toLowerCase();

        Query query = new QueryBuilder(CommandType.SELECT)
                .addTable(tableName)
                .addColumn("id","")
                .addCondition(column + "=" + foreignKey)
                .build();
        CachedRowSet set = executor.execute(query).orElseThrow(SQLException::new);
        if (set.next()) {
            return set.getInt(1);
        }
        return null;
    }
}
