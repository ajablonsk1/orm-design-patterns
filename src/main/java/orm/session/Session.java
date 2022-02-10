package orm.session;

import orm.annotations.OneToOne;
import orm.schema.ClassFinder;
import orm.schema.ClassScanner;
import orm.session.operations.ObjectLoader;
import orm.session.operations.ObjectSaver;
import orm.session.operations.ObjectUpdater;
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
    private ObjectSaver objectSaver;
    private ObjectUpdater objectUpdater;

    // konstruktor package-private, aby tylko klasa SessionFactory mogła tworzyć obiekty klasy Session
    Session(ConnectionPool connectionPool) throws Exception {
        executor = new Executor(connectionPool);
        idGiver = new IdGiver(connectionPool);
        classFinder = new ClassFinder();
        classScanner = new ClassScanner();
        objectLoader = new ObjectLoader(executor, identityMap);
        objectSaver = new ObjectSaver(executor, identityMap, objectsToSave, idGiver);
        objectUpdater = new ObjectUpdater(objectsToUpdate, executor);
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


    private void flushDelete() {
        for(Object object: objectsToDelete){

        }

        // TODO
    }

    public void flush(){
        objectSaver.saveAll();
        objectUpdater.updateAll();
        flushDelete();

        objectsToSave.clear();
        objectsToDelete.clear();
        objectsToUpdate.clear();
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
