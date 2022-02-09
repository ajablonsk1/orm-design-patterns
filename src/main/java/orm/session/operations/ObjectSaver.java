package orm.session.operations;

import orm.annotations.OneToOne;
import orm.schema.ClassScanner;
import orm.session.Executor;
import orm.sql.CommandType;
import orm.sql.IdGiver;
import orm.sql.Query;
import orm.sql.QueryBuilder;

import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ObjectSaver {

    private final IdGiver idGiver;
    private ClassScanner classScanner = new ClassScanner();
    private Executor executor;
    private Map<Integer, Object> identityMap;
    private Set<Object> objectsToSave;

    public ObjectSaver(Executor executor, Map<Integer, Object> identityMap, Set<Object> objectsToSave, IdGiver idGiver){
        this.executor = executor;
        this.identityMap = identityMap;
        this.objectsToSave = objectsToSave;
        this.idGiver = idGiver;
    }

    private void insertRecord(Class<?> clazz, Object object){
        QueryBuilder qb = new QueryBuilder(CommandType.INSERT);
        try {
            qb.addTable(clazz);
            qb.addColumn("id", "").addValue(getObjectId(object));
            for (Field column : classScanner.getColumns(clazz)) {
                qb.addColumn(column).addValue(column.get(object));
            }

            for (Field field : classScanner.getOneToOneFields(clazz)) {
                if (field.getAnnotation(OneToOne.class).foreignKeyInThisTable()) {
                    String columnName = field.getName() + "_id";
                    qb.addColumn(columnName, null);
                    qb.addValue(null);
                    //qb.addValue(getObjectId(field.get(object)));
                }
            }

            for (Field field : classScanner.getManyToOneFields(clazz)) {
                String columnName = field.getName() + "_id";
                qb.addColumn(columnName, null);
                qb.addValue(null);
                //qb.addValue(getObjectId(field.get(object)));
            }

        } catch (IllegalAccessException e){
            e.printStackTrace();
        }

        Query query = qb.build();
        executor.execute(query);
    }

    private void setForeignKeys(Class<?> cl, Object object) {
        QueryBuilder qb = new QueryBuilder(CommandType.UPDATE);

        try {
            for (Field field : classScanner.getOneToOneFields(cl)) {
                if (field.getAnnotation(OneToOne.class).foreignKeyInThisTable()) {
                    qb = new QueryBuilder(CommandType.UPDATE);
                    qb.addTable(cl);
                    String columnName = field.getName() + "_id";
                    qb.setColumn(columnName, getObjectId(field.get(object)));
                    qb.addCondition("id = " + getObjectId(object));
                    executor.execute(qb.build());
                }
            }

            for (Field field : classScanner.getManyToOneFields(cl)) {
                qb = new QueryBuilder(CommandType.UPDATE);
                qb.addTable(cl);
                String columnName = field.getName() + "_id";
                qb.setColumn(columnName, getObjectId(field.get(object)));
                qb.addCondition("id = " + getObjectId(object));
                executor.execute(qb.build());
            }

        } catch (IllegalAccessException e){
            e.printStackTrace();
        }
    }

    public void saveAll() {
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

        for (Object object: objectsToSave) {
            setForeignKeys(object.getClass(), object);
            for (Class<?> clazz : classScanner.getParentEntityClasses(object.getClass()))
                setForeignKeys(clazz, object);
        }
    }

    private void setObjectId(Object object, int id) throws IllegalAccessException {
        classScanner.getIdField(object.getClass()).set(object, id);
    }

    private int getObjectId(Object object) throws IllegalAccessException {
        return (int) classScanner.getIdField(object.getClass()).get(object);
    }
}
