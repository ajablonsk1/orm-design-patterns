package orm.session.operations;

import orm.annotations.ManyToMany;
import orm.annotations.ManyToOne;
import orm.annotations.OneToMany;
import orm.annotations.OneToOne;
import orm.schema.reflect.ClassScanner;
import orm.session.Executor;
import orm.session.finder.QueryObject;
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
    private final Executor executor;
    private final Map<Integer, Object> identityMap;
    private final Map<Integer, Object> objectsBeingLoaded = new HashMap<>();
    private final ClassScanner classScanner = new ClassScanner();

    public ObjectLoader(Executor executor, Map<Integer, Object> identityMap){
        this.executor = executor;
        this.identityMap = identityMap;
    }
    public List<Object> loadQuery(QueryObject qo) throws SQLException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        Query query = qo.build();

        CachedRowSet cachedRowSet = executor.execute(query)
                .orElseThrow(SQLException::new);
        Class<?> clazz = qo.getClazz();

        List<Object> container = new ArrayList<>();
        while (cachedRowSet.next()){
            Integer ids = cachedRowSet.getInt("id");
            container.add(load(clazz, ids));
        }
        return container;
    }

    public Object load(Class<?> clazz, Integer id) throws NoSuchMethodException, SQLException, InvocationTargetException, InstantiationException, IllegalAccessException {
        //sprawdz czy juz byl wczytany
        if (! objectsBeingLoaded.containsKey(id) && identityMap.containsKey(id))
            return identityMap.get(id);

        if (id == 0){
            return null;
        }

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
            else if (field.isAnnotationPresent(ManyToMany.class)){
                fieldValue = getManyToManyFieldValue(clazz,id,field);

            }else {
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
            if (otherId == 0){
                return null;
            }
            fieldValue = load(fieldType, otherId);
        } catch (Exception e){
            e.printStackTrace();
        }
        return fieldValue;
    }

    private void setField(Object object, Field field, Object fieldValue) throws IllegalAccessException {

        field.setAccessible(true);
        field.set(object,fieldValue);
        field.setAccessible(false);
    }

    private Integer findKey(String tableName, Integer foreignKey, String column) throws Exception {

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

        throw new Exception("Record referenced by foreign key does not exist");
    }
    private Object getManyToManyFieldValue(Class<?> clazz, Integer id, Field field) throws SQLException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        Object fieldValue;
        String junctionTable = field.getAnnotation(ManyToMany.class).tableName();


        ParameterizedType pType = (ParameterizedType) field.getGenericType();
        Class<?> otherClass = (Class<?>) pType.getActualTypeArguments()[0];

        Query query = new QueryBuilder(CommandType.SELECT).addTable(junctionTable)
                .addColumn(otherClass.getSimpleName().toLowerCase() +"_id","")
                .addCondition(clazz.getSimpleName().toLowerCase() + "_id = " + id)
                .build();

        CachedRowSet set = executor.execute(query).orElseThrow(SQLException::new);

        Collection<Object> container = new ArrayList<>();

        while (set.next()){
            Integer ids = set.getInt(1);
            container.add(load(otherClass, ids));
        }
        fieldValue = container;
        return fieldValue;

    }

}
