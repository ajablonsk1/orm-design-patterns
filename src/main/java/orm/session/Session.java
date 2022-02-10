package orm.session;

import orm.annotations.OneToOne;
import orm.annotations.ManyToMany;
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
        List<Query> queries = new ArrayList<>();
        try {
            for (Object obj : objectsToDelete) {
                Class cl = obj.getClass();
                int id = getObjectId(obj);
                identityMap.remove(id);

                deleteForClass(obj, obj.getClass());

                for (Class parent : classScanner.getParentEntityClasses(cl)) {
                    deleteForClass(obj, parent);
                    QueryBuilder qb = new QueryBuilder(CommandType.DELETE);
                    qb.addTable(parent);
                    qb.addCondition("id = " + id);
                    queries.add(qb.build());
                }

                QueryBuilder qb = new QueryBuilder(CommandType.DELETE);
                qb.addTable(cl);
                qb.addCondition("id = " + id);
                queries.add(qb.build());
                executor.execute(queries);
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private void deleteForClass(Object obj, Class cl) throws IllegalAccessException {
        List<Query> queries = new ArrayList<>();
        int id = getObjectId(obj);

        for (Field field : classScanner.getOneToOneFields(cl)) {
            field.setAccessible(true);
            Object fieldValue = field.get(obj);
            if (fieldValue == null)
                continue;
            int fieldId = getObjectId(fieldValue);
            if (field.getAnnotation(OneToOne.class).foreignKeyInThisTable()) {
                QueryBuilder qb = new QueryBuilder(CommandType.UPDATE);
                qb.addTable(cl);
                qb.setColumn(field.getName().toLowerCase()+"_id", null);
                qb.addCondition("id = " + id);
                queries.add(qb.build());
            } else {
                for (Field field1 : classScanner.getOneToOneFields(fieldValue.getClass())) {
                    field1.setAccessible(true);
                    if (field1.getAnnotation(OneToOne.class).foreignKeyInThisTable() && id == getObjectId(field1.get(fieldValue))) {
                        QueryBuilder qb = new QueryBuilder(CommandType.UPDATE);
                        qb.addTable(fieldValue.getClass());
                        qb.setColumn(field1.getName().toLowerCase()+"_id", null);
                        qb.addCondition("id = " + fieldId);
                        queries.add(qb.build());
                    }
                    field1.setAccessible(false);
                }
            }
            field.setAccessible(false);
        }

        for (Field field : classScanner.getOneToManyFields(cl)) {
            field.setAccessible(true);
            Collection collection = (Collection) field.get(obj);
            if (collection == null)
                continue;
            for (Object fieldValue : collection) {
                if (fieldValue == null)
                    continue;
                int fieldId = getObjectId(fieldValue);
                QueryBuilder qb = new QueryBuilder(CommandType.UPDATE);
                qb.addTable(fieldValue.getClass());
                String columnName = null;
                for (Field field1 : classScanner.getManyToOneFields(fieldValue.getClass())) {
                    field1.setAccessible(true);
                    Object field1Value = field1.get(fieldValue);
                    if (field1Value == null)
                        continue;
                    if (id == getObjectId(field1Value)) {
                        columnName = field1.getName().toLowerCase() + "_id";
                    }
                    field1.setAccessible(false);
                }
                qb.setColumn(columnName, null);
                qb.addCondition("id = " + fieldId);
                queries.add(qb.build());
            }
            field.setAccessible(false);
        }

        for (Field field : classScanner.getManyToOneFields(cl)) {
            field.setAccessible(true);
            Object fieldValue = field.get(obj);
            if (fieldValue == null)
                continue;
            int fieldId = getObjectId(fieldValue);
            QueryBuilder qb = new QueryBuilder(CommandType.UPDATE);
            qb.addTable(cl);
            qb.setColumn(field.getName().toLowerCase()+"_id", null);
            qb.addCondition("id = " + id);
            queries.add(qb.build());
            field.setAccessible(false);
        }

        for (Field field : classScanner.getManyToManyFields(cl)) {
            System.out.println(cl);
            System.out.println(field.getName());
            field.setAccessible(true);
            Collection collection = (Collection) field.get(obj);
            if (collection == null)
                continue;
            for (Object fieldValue : collection) {
                if (fieldValue == null)
                    continue;
                int fieldId = getObjectId(fieldValue);
                String tableName = field.getAnnotation(ManyToMany.class).tableName();
                QueryBuilder qb = new QueryBuilder(CommandType.DELETE);
                qb.addTable(tableName);
                qb.addCondition(cl.getSimpleName().toLowerCase() + "_id" + " = " + id);
                qb.addCondition(getElementClass(field).getSimpleName().toLowerCase() + "_id" + " = " + fieldId);
                queries.add(qb.build());
            }
            field.setAccessible(false);
        }
        executor.execute(queries);
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

    private Class getElementClass(Field field) {
        Type type = field.getGenericType();
        Class ret;
        if (type instanceof ParameterizedType) {
            Type[] typeArguments = ((ParameterizedType) type).getActualTypeArguments();
            if (typeArguments.length == 0)
                throw new IllegalArgumentException("Field not parametrised by any type");
            if (typeArguments.length != 1) {
                throw new IllegalArgumentException("Field parametrised by more than one type");
            }
            if (!(typeArguments[0] instanceof Class)) {
                throw new IllegalArgumentException("Field not parametrised by Class");
            }
            ret = (Class) typeArguments[0];
        } else {
            throw new IllegalArgumentException("Field not parametrised by any type");
        }
        return ret;
    }
}
