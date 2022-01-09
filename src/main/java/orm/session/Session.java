package orm.session;

import orm.annotations.OneToOne;
import orm.session.Executor;
import orm.schema.ClassFinder;
import orm.schema.ClassScanner;
import orm.sql.CommandType;
import orm.sql.Query;
import orm.sql.QueryBuilder;

import javax.sql.rowset.CachedRowSet;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.*;

public class Session {
    private Executor executor;
    private Map<Integer, Object> identityMap = new HashMap<>();
    private Set<Object> objectsToUpdate = new HashSet<>();
    private Set<Object> objectsToDelete = new HashSet<>();
    private Set<Object> objectsToSave = new HashSet<>();
    private ClassFinder classFinder;
    private ClassScanner classScanner;

    // konstruktor package-private, aby tylko klasa SessionFactory mogła tworzyć obiekty klasy Session
    Session() throws Exception {
        //TODO: parametry połączenia przez plik konfiguracyjny
        this.executor = new Executor("jdbc:mysql://mysql.agh.edu.pl:3306", "pholowi1", "Qo1J2fAHMTSADYdW", "pholowi1");
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

        if (identityMap.containsKey(id)) return identityMap.get(id);
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
            //System.out.println(field.getName().toLowerCase());
            Class<?> fieldType = field.getType();
            Object fieldValue = null;

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
                    System.out.println(fieldType);
                    fieldValue = load(fieldType,idA);
                }
            }else {
                //Only primitive
                fieldValue = cachedRowSet.getObject(field.getName().toLowerCase());
            }


            setField(instance,field,fieldValue);
        }



        return instance;
    }
    private Integer findKey(String tableName,Integer foreignKey,String columName ) throws SQLException {
        String[] array = columName.split("\\.");
        columName = array[array.length-1].toLowerCase();
        System.out.println("table name: " + tableName);

        System.out.println("column name : " + columName);

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
        //TODO
    }

    public void delete(Object object) {
        //TODO
    }

    private void updateId(Integer id) {
        QueryBuilder queryBuilder = new QueryBuilder(CommandType.UPDATE);
        Query query = queryBuilder.addTable("id")
                .setColumn("id", id+1)
                .build();
        executor.execute(query);
    }

    private Integer getId() throws SQLException {
        QueryBuilder queryBuilder = new QueryBuilder(CommandType.SELECT);
        Query query = queryBuilder.addColumn("id", "")
                .addTable("id")
                .build();
        Optional<CachedRowSet> crs = executor.execute(query);
        if(crs.isPresent()){
            return crs.get().getInt(1);
        }
        else{
            throw new IllegalStateException("Cannot take id record");
        }
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
                    int id = getId();
                    identityMap.put(id, object);
                    updateId(id);
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
