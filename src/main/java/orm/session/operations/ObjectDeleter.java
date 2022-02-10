package orm.session.operations;

import orm.annotations.ManyToMany;
import orm.annotations.OneToOne;
import orm.schema.ClassScanner;
import orm.session.Executor;
import orm.sql.CommandType;
import orm.sql.Query;
import orm.sql.QueryBuilder;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

public class ObjectDeleter {
    private Executor executor;
    private Map<Integer, Object> identityMap;
    private Set<Object> objectsToDelete;
    private ClassScanner classScanner = new ClassScanner();

    public ObjectDeleter(Executor executor, Map<Integer, Object> identityMap, Set<Object> objectsToDelete){
        this.executor = executor;
        this.identityMap = identityMap;
        this.objectsToDelete = objectsToDelete;
    }

    public void deleteAll()  {
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

        getQueriesForOneToOne(obj, cl, id, queries);
        getQueriesForOneToMany(obj, cl, id, queries);
        getQueriesForManyToOne(obj, cl, id, queries);
        getQueriesForManyToMany(obj, cl, id, queries);

        executor.execute(queries);
    }

    private void getQueriesForOneToOne(Object obj, Class cl, int id, List<Query> queries) throws IllegalAccessException {
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
    }

    private void getQueriesForOneToMany(Object obj, Class cl, int id, List<Query> queries) throws IllegalAccessException {
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
    }

    private void getQueriesForManyToOne(Object obj, Class cl, int id, List<Query> queries) throws IllegalAccessException {
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
    }

    private void getQueriesForManyToMany(Object obj, Class cl, int id, List<Query> queries) throws IllegalAccessException {
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
    }

    private int getObjectId(Object object) throws IllegalAccessException {
        return (int) classScanner.getIdField(object.getClass()).get(object);
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
