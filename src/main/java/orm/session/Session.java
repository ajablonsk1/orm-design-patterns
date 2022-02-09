package orm.session;

import orm.schema.ClassFinder;
import orm.schema.ClassScanner;
import orm.sql.CommandType;
import orm.sql.IdGiver;
import orm.sql.Query;
import orm.sql.QueryBuilder;

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
    private ObjectLoader objectLoader;

    // konstruktor package-private, aby tylko klasa SessionFactory mogła tworzyć obiekty klasy Session
    Session(ConnectionPool connectionPool) throws Exception {
        executor = new Executor(connectionPool);
        idGiver = new IdGiver(connectionPool);
        classFinder = new ClassFinder();
        classScanner = new ClassScanner();
        objectLoader = new ObjectLoader(executor, identityMap);
    }

    public void save(Object object) {
        objectsToSave.add(object);
    }

    public Object load(Class<?> clazz, Integer id) throws Exception {
        return objectLoader.load(clazz, id);
    }

    public void update(Object object) {
        objectsToUpdate.add(object);
    }

    public void delete(Object object) {
        objectsToDelete.add(object);
    }

    private void insertRecord(Class<?> clazz, Object object){
        QueryBuilder qb = new QueryBuilder(CommandType.INSERT);
        qb.addTable(clazz);

        try {
            for (Field column : classScanner.getAnnotatedFields(clazz)) {
                qb.addColumn(column).addValue(column.get(object));
            }
            qb.addColumn("id", "INT").addValue(getObjectId(object));
        } catch (IllegalAccessException e){
            e.printStackTrace();
        }

        Query query = qb.build();
        executor.execute(query);
    }

    private void flushSave() {
        for (Object object: objectsToSave){
            try {
                if (!identityMap.containsValue(object)) {
                    int id = idGiver.getId();
                    identityMap.put(id, object);
                    setObjectId(object, id);
                }
            } catch (SQLException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        for (Object object: objectsToSave) {
            insertRecord(object.getClass(), object);
            for (Class<?> clazz : classScanner.getParentEntityClasses(object.getClass()))
                insertRecord(clazz, object);
        }
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

    private void setObjectId(Object object, int id) throws IllegalAccessException {
        classScanner.getIdField(object.getClass()).set(object, id);
    }

    private int getObjectId(Object object) throws IllegalAccessException {
        return (int) classScanner.getIdField(object.getClass()).get(object);
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
}
