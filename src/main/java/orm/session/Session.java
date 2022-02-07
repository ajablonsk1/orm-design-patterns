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
    public Object load(Class<?> clazz,Integer id) throws NoSuchMethodException, SQLException {
        //sprawdz czy juz byl wczytany

        if (identityMap.containsKey(id)){
            return identityMap.get(id);
        }
        //wczytaj dane z obiektu


        QueryBuilder queryBuilder = new QueryBuilder(CommandType.SELECT);
        Query query = queryBuilder.addTable(clazz)
                .addCondition("id = " + id)
                .build();


        // jakis throw czy cos
        CachedRowSet cachedRowSet = executor.execute(query).orElseThrow(
                SQLException::new);




        //wez wszystkie pola z adnotacjami do ustawienia
        //TODO metoda w classfinder tu zle
        Field[] annotatedFields = clazz.getDeclaredFields ();
        AccessibleObject.setAccessible (annotatedFields, true);

        Object instance = null;
        try {
            instance = clazz.getDeclaredConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        identityMap.put(id,instance);

        //move cursor
        cachedRowSet.next();

        for (Field field: annotatedFields){

            Class<?> fieldType = field.getType();
            Object fieldValue = null;
            //System.out.println(fieldType);

            //OnetoOne case
            if (field.isAnnotationPresent(OneToOne.class)){

                OneToOne annotation = field.getAnnotation(OneToOne.class);
                boolean foreignKeyInThisTable = annotation.foreignKeyInThisTable();

                String[] array = fieldType.toString().split("\\.");
                String entityName = array[array.length-1].toLowerCase();


                if (foreignKeyInThisTable){
                    Integer foreignKey = cachedRowSet.getInt(entityName + "_id");
                    fieldValue = load(fieldType,foreignKey);
                }else{
                    //TODO zmiana nazwy
                    Integer idA = findKey(entityName,id,clazz.toString().toLowerCase()+"_id");
                    //System.out.println(fieldType);
                    fieldValue = load(fieldType,idA);
                }
            }else if (field.isAnnotationPresent(OneToMany.class)) {
                //tu jest lista generic daje zwraca x List<x>
                Type genericFieldType = field.getGenericType();


                String[] array = genericFieldType.toString().split("[.>]");
                String entityNameToLoad = array[array.length-1].toLowerCase();

                //getThose"Manys"
                //get from database
                QueryBuilder queryBuilder1 = new QueryBuilder(CommandType.SELECT);
                //class name with orm.test.class
                //parse

                Query query1 = queryBuilder1.addTable(entityNameToLoad)
                        .addColumn("id","")
                        .addCondition(clazz.getSimpleName().toLowerCase() +"_id = " +id)
                        .build();


                CachedRowSet set = executor.execute(query1).orElseThrow(
                        SQLException::new);



                //TODO nie obslugujemy hashsetow jakby komus sie zachcialo
                Collection<Object> container = new ArrayList<>();
                while (set.next()){
                    Integer ids = set.getInt("id");
                    ParameterizedType pType = (ParameterizedType) genericFieldType;
                    Class<?> collectionType = (Class<?>) pType.getActualTypeArguments()[0];

                    container.add(load(collectionType,ids));
                }


                fieldValue = container;


            }else if (field.isAnnotationPresent(ManyToOne.class))
            {

                Integer foreignKey = cachedRowSet.getInt(fieldType.getSimpleName() + "_id");
                fieldValue = load(fieldType,foreignKey);

            }else{
                //Only primitive
                fieldValue = cachedRowSet.getObject(field.getName().toLowerCase());
            }


            setField(instance,field,fieldValue);
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
    private Integer findKey(String tableName,Integer foreignKey,String columName ) throws SQLException {
        String[] array = columName.split("\\.");
        columName = array[array.length-1].toLowerCase();
//        System.out.println("table name: " + tableName);
//
//        System.out.println("column name : " + columName);

        QueryBuilder queryBuilder = new QueryBuilder(CommandType.SELECT);
        Query query = queryBuilder
                .addTable(tableName)
                .addColumn("id","")
                .addCondition(columName + "=" + foreignKey)
                .build();
        CachedRowSet set = executor.execute(query).orElseThrow(SQLException::new);
        if(set.next()) {
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
