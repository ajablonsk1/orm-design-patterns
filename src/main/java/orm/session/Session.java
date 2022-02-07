package orm.session;

import orm.annotations.ManyToOne;
import orm.annotations.OneToMany;
import orm.annotations.OneToOne;
import orm.schema.ClassFinder;
import orm.schema.ClassScanner;
import orm.sql.CommandType;
import orm.sql.IdGiver;
import orm.sql.Query;
import orm.sql.QueryBuilder;

import javax.sql.rowset.CachedRowSet;
import java.lang.reflect.*;
import java.sql.SQLException;
import java.util.*;

public class Session {
    private Executor executor;
    private IdGiver idGiver;
    private Map<Integer, Object> identityMap = new HashMap<>();
    private Set<Object> objectsToUpdate = new HashSet<>();
    private Set<Object> objectsToDelete = new HashSet<>();
    private Set<Object> objectsToSave = new HashSet<>();
    private ClassFinder classFinder;
    private ClassScanner classScanner;

    // konstruktor package-private, aby tylko klasa SessionFactory mogła tworzyć obiekty klasy Session
    Session(ConnectionPool connectionPool) throws Exception {
        //TODO: parametry połączenia przez plik konfiguracyjny
        executor = new Executor(connectionPool);
        idGiver = new IdGiver(connectionPool);
        classFinder = new ClassFinder();
        classScanner = new ClassScanner();
    }

    public void save(Object object) {
        //Nie musze sprawdzac czy obiekt jest w zbiorze, poniewaz jezeli jest to sie nie doda (jako ze mamy HashSet)
        objectsToSave.add(object);
    }

    //TODO refactor
    public Object load(Class<?> clazz, Integer id) throws NoSuchMethodException, SQLException, InvocationTargetException, InstantiationException, IllegalAccessException {
        //sprawdz czy juz byl wczytany
        if (identityMap.containsKey(id))
            return identityMap.get(id);

        // wez wszystkie pola z adnotacjami do ustawienia
        Field[] annotatedFields = classScanner.getAnnotatedFields(clazz).toArray(new Field[0]);
        AccessibleObject.setAccessible(annotatedFields, true);

        Object instance = clazz.getDeclaredConstructor().newInstance();

        // wczytaj pola obiektu z bazy danych
        Query query = new QueryBuilder(CommandType.SELECT).addTable(clazz)
                            .addCondition("id = " + id).build();
        CachedRowSet cachedRowSet = executor.execute(query)
                            .orElseThrow(SQLException::new);
        cachedRowSet.next();

        // już tutaj wstawiamy do identityMap ze względu na kaskadowe ładowanie
        identityMap.put(id, instance);

        for (Field field: annotatedFields){
            Class<?> fieldType = field.getType();
            Object fieldValue = null;

            if (field.isAnnotationPresent(OneToOne.class)){
                String[] a = fieldType.toString().split("\\.");
                String entityName = a[a.length-1].toLowerCase();
                Integer otherId = field.getAnnotation(OneToOne.class).foreignKeyInThisTable()
                                    ? cachedRowSet.getInt(entityName + "_id")
                                    : findKey(entityName, id,clazz.toString().toLowerCase()+"_id");
                fieldValue = load(fieldType, otherId);
            }
            else if (field.isAnnotationPresent(OneToMany.class)) {
                //tu jest lista generic zwraca x List<x>
                String[] a = field.getGenericType().toString().split("[.>]");
                String entityNameToLoad = a[a.length-1].toLowerCase();

                // build query to get a list of items
                Query query1 = new QueryBuilder(CommandType.SELECT).addTable(entityNameToLoad)
                                .addColumn("id","")
                                .addCondition(clazz.getSimpleName().toLowerCase() + "_id = " +id)
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
            }
            else if (field.isAnnotationPresent(ManyToOne.class)) {
                Integer foreignKey = cachedRowSet.getInt(fieldType.getSimpleName() + "_id");
                fieldValue = load(fieldType,foreignKey);
            }
            else {
                //Only primitive
                fieldValue = cachedRowSet.getObject(field.getName().toLowerCase());
            }
            setField(instance, field, fieldValue);
        }

        return instance;
    }

//    private Object OneToOneLoad(Field field,CachedRowSet cachedRowSet) throws SQLException, NoSuchMethodException {
////
////        OneToOne annotation = field.getAnnotation(OneToOne.class);
////        boolean foreignKeyInThisTable = annotation.foreignKeyInThisTable();
////
////        Class<?> fieldType = field.getType();
////        String entityName = fieldType.getSimpleName();
////
////        if (foreignKeyInThisTable){
////            Integer foreignKey = cachedRowSet.getInt(entityName + "_id");
////            return load(fieldType,foreignKey);
////        }else{
////            //TODO zmiana nazwy
////            Integer idA = findKey(entityName,id,clazz.toString().toLowerCase()+"_id");
////            //System.out.println(fieldType);
////            fieldValue = load(fieldType,idA);
////        }
////    }

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

    public void update(Object object) {
        objectsToUpdate.add(object);
    }

    public void delete(Object object) {
        objectsToDelete.add(object);
    }

    private void insertObjectRecord(Object object){
        QueryBuilder queryBuilder = new QueryBuilder(CommandType.INSERT);
        List<Field> columns = classScanner.getColumns(object.getClass());
        queryBuilder.addTable(object.getClass());
        columns.forEach(column -> {
            try {
                queryBuilder.addValue(column.get(object));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        });
        Query query = queryBuilder.build();
        executor.execute(query);
    }

    private void insertClassRecord(Class clazz, Object object){
        QueryBuilder queryBuilder = new QueryBuilder(CommandType.INSERT);
        //TODO a to nie wpisze tylko tylko @Column a co z relacjami?
        List<Field> columns = classScanner.getColumns(clazz);
        queryBuilder.addTable(clazz);
        columns.forEach(column -> {
            try {
                queryBuilder.addValue(column.get(object));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        });
        Query query = queryBuilder.build();
        executor.execute(query);
    }

    private void flushSave(){
        objectsToSave.forEach(object -> {
            if(!identityMap.containsValue(object)){
                List<Class> classes = classScanner.getParentEntityClasses(object.getClass());
                classes.forEach(clazz -> {
                    insertClassRecord(clazz, object);
                });
                try {
                    int id = idGiver.getId();
                    identityMap.put(id, object);
                    insertObjectRecord(object);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void flushUpdate() {
        //TODO
    }

    private void flushDelete() {
        //TODO
        //config

    }

    public void flush(){
        flushSave();
        flushUpdate();
        flushDelete();
    }

    private boolean setField(Object object,Field field,Object fieldValue){

        //TODO dziedziczenie
        try {
            field.set(object,fieldValue);
            return true;
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return false;
    }
}
