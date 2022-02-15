package orm.session;

import orm.session.operations.ObjectDeleter;
import orm.session.operations.ObjectLoader;
import orm.session.operations.ObjectSaver;
import orm.session.operations.ObjectUpdater;

import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.*;

public class Session {
    private final Set<Object> objectsToUpdate = new HashSet<>();
    private final Set<Object> objectsToDelete = new HashSet<>();
    private final Set<Object> objectsToSave = new HashSet<>();

    private final ObjectLoader objectLoader;
    private final ObjectSaver objectSaver;
    private final ObjectUpdater objectUpdater;
    private final ObjectDeleter objectDeleter;

    // konstruktor package-private, aby tylko klasa SessionFactory mogła tworzyć obiekty klasy Session
    Session(ConnectionPool connectionPool) {
        Executor executor = new Executor(connectionPool);
        IdGiver idGiver = new IdGiver(connectionPool);
        Map<Integer, Object> identityMap = new HashMap<>();
        objectLoader = new ObjectLoader(executor, identityMap);
        objectSaver = new ObjectSaver(executor, identityMap, objectsToSave, idGiver);
        objectUpdater = new ObjectUpdater(objectsToUpdate, executor, objectSaver);
        objectDeleter = new ObjectDeleter(executor, identityMap, objectsToDelete);
    }

    public void save(Object object) {
        objectsToSave.add(object);
    }

    public Object load(Class<?> clazz, Integer id) {
        try {
            return objectLoader.load(clazz, id);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void update(Object object) {
        objectsToUpdate.add(object);
    }

    public void delete(Object object) {
        objectsToDelete.add(object);
    }

    public void flush(){
        objectSaver.saveAll();
        objectUpdater.updateAll();
        objectDeleter.deleteAll();

        objectsToSave.clear();
        objectsToDelete.clear();
        objectsToUpdate.clear();
    }

}
