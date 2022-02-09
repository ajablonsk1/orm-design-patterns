package orm.session;

import orm.annotations.OneToOne;
import orm.annotations.ManyToMany;
import orm.schema.ClassFinder;
import orm.schema.ClassScanner;
import orm.session.operations.ObjectLoader;
import orm.session.operations.ObjectSaver;
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

    // konstruktor package-private, aby tylko klasa SessionFactory mogła tworzyć obiekty klasy Session
    Session(ConnectionPool connectionPool) throws Exception {
        executor = new Executor(connectionPool);
        idGiver = new IdGiver(connectionPool);
        classFinder = new ClassFinder();
        classScanner = new ClassScanner();
        objectLoader = new ObjectLoader(executor, identityMap);
        objectSaver = new ObjectSaver(executor, identityMap, objectsToSave, idGiver);
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

    private void updateRecord(Class<?> cl, Object object) {
        try {
            if (! classScanner.getColumns(cl).isEmpty()) {
                QueryBuilder qb = new QueryBuilder(CommandType.UPDATE);
                qb.addTable(cl);

                for (Field column : classScanner.getColumns(cl)) {
                    qb.setColumn(column, column.get(object));
                    qb.addCondition("id = " + getObjectId(object));
                }
                executor.execute(qb.build());
            }

            for (Field field : classScanner.getOneToOneFields(cl)) {
                if (field.getAnnotation(OneToOne.class).foreignKeyInThisTable()) {
                    QueryBuilder qb = new QueryBuilder(CommandType.UPDATE);
                    qb.addTable(cl);
                    String columnName = field.getName() + "_id";
                    qb.setColumn(columnName, getObjectId(field.get(object)));
                    qb.addCondition("id = " + getObjectId(object));
                    executor.execute(qb.build());
                }
            }

            for (Field field : classScanner.getManyToOneFields(cl)) {
                QueryBuilder qb = new QueryBuilder(CommandType.UPDATE);
                qb.addTable(cl);
                String columnName = field.getName() + "_id";
                qb.setColumn(columnName, getObjectId(field.get(object)));
                qb.addCondition("id = " + getObjectId(object));
                executor.execute(qb.build());
            }

            // update Many-to-Many

//            for (Field field : classScanner.getManyToManyFields(cl)){
//                String tableName = field.getAnnotation(ManyToMany.class).tableName();
//
//                // TODO wyciągnąć do zewnętrznej metody i sprawdzić czy działa w teście jednostkowym
//                Class<?> otherClass = (Class<?>) ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];
//
//                String thisColumnName = classScanner.getManyToManyFields(otherClass)
//                                            .stream()
//                                            .filter(f -> f.getAnnotation(ManyToMany.class).tableName() == tableName)
//                                            .reduce((first, second) -> first)
//                                            .get()
//                                            .getName().toLowerCase() + "_id";
//
//                String otherColumnName = field.getName().toLowerCase() + "_id";
//
//                // select from association table
//                QueryBuilder qb2 = new QueryBuilder(CommandType.SELECT);
//                qb2.addTable(tableName)
//                        .addColumn(thisColumnName, "")
//                        .addColumn(otherColumnName, "");

                // find records to insert

                // find records to delete
           // }


        } catch (IllegalAccessException e){
            e.printStackTrace();
        }

    }

    private void flushUpdate() {
        for (Object object : objectsToUpdate){
            updateRecord(object.getClass(), object);
            for (Class<?> clazz : classScanner.getParentEntityClasses(object.getClass()))
                updateRecord(clazz, object);
        }
        // TODO przy OneToMany update powinien dziać się z drugiej strony?
    }

    private void flushDelete() {
        List<Query> queries = new ArrayList<>();
        try {
            for (Object obj : objectsToDelete) {
                Class cl = obj.getClass();
                int id = getObjectId(obj);
                identityMap.remove(id);

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
                    field.setAccessible(true);
                    Object fieldValue = field.get(obj);
                    if (fieldValue == null)
                        continue;
                    int fieldId = getObjectId(fieldValue);
                    String tableName = field.getAnnotation(ManyToMany.class).tableName();
                    QueryBuilder qb = new QueryBuilder(CommandType.DELETE);
                    qb.addTable(cl);
                    qb.addCondition(cl.getSimpleName().toLowerCase() +"_id" + " = " + id);
                    qb.addCondition(fieldValue.getClass().getSimpleName().toLowerCase() +"_id" + " = " + fieldId);
                    queries.add(qb.build());
                    field.setAccessible(false);
                }

                for (Class parent : classScanner.getParentEntityClasses(cl)) {
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

    public void flush(){
        objectSaver.saveAll();
        flushUpdate();
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
