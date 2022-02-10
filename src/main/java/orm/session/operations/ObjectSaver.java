package orm.session.operations;

import orm.annotations.ManyToMany;
import orm.annotations.OneToOne;
import orm.schema.ClassScanner;
import orm.session.Executor;
import orm.sql.CommandType;
import orm.session.IdGiver;
import orm.sql.Query;
import orm.sql.QueryBuilder;

import javax.sql.rowset.CachedRowSet;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

public class ObjectSaver {

    private final IdGiver idGiver;
    private final ClassScanner classScanner = new ClassScanner();
    private final Executor executor;
    private final Map<Integer, Object> identityMap;
    private final Set<Object> objectsToSave;
    private final IdService idService = new IdService();

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
            qb.addColumn("id", "").addValue(idService.getObjectId(object));
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


        try {
            for (Field field : classScanner.getOneToOneFields(cl)) {
                if (field.getAnnotation(OneToOne.class).foreignKeyInThisTable()) {
                    updateForeignKeyValue(cl, object, field);
                }
            }

            for (Field field : classScanner.getManyToOneFields(cl)) {
                updateForeignKeyValue(cl, object, field);
            }

        } catch (IllegalAccessException e){
            e.printStackTrace();
        }
    }

    private void updateForeignKeyValue(Class<?> cl, Object object, Field field) throws IllegalAccessException {
        QueryBuilder qb = new QueryBuilder(CommandType.UPDATE);
        qb.addTable(cl);
        String columnName = field.getName() + "_id";
        qb.setColumn(columnName, idService.getObjectId(field.get(object)));
        qb.addCondition("id = " + idService.getObjectId(object));
        executor.execute(qb.build());
    }


    public void saveAll() {
        for (Object object: objectsToSave){
            try {
                if (!identityMap.containsValue(object)) {
                    int id = idGiver.getId();
                    identityMap.put(id, object);
                    idService.setObjectId(object, id);
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
            insertManyToManys(object.getClass(), object);
            for (Class<?> clazz : classScanner.getParentEntityClasses(object.getClass())){
                setForeignKeys(clazz, object);
                insertManyToManys(clazz, object);
            }
        }
    }

    private void insertManyToManys(Class<?> cl, Object object) {
        try {
            for (Field field : classScanner.getManyToManyFields(cl)) {
                String tableName = field.getAnnotation(ManyToMany.class).tableName();
                String thisColumn = cl.getSimpleName().toLowerCase() + "_id";
                String otherColumn = AssociationTableService.getColumnNameForField(field);

                AccessibleObject.setAccessible(new AccessibleObject[]{field}, true);

                for (Object otherObj : (Collection<?>) field.get(object)) {
                    // check if record is already inserted
                    QueryBuilder qb = new QueryBuilder(CommandType.SELECT)
                            .addColumn(thisColumn, "")
                            .addColumn(otherColumn, "")
                            .addTable(tableName)
                            .addCondition(thisColumn + " = " + idService.getObjectId(object))
                            .addCondition(otherColumn + " = " + idService.getObjectId(otherObj));
                    CachedRowSet crs = executor.execute(qb.build()).orElseThrow();

                    //insert if not inserted
                    if (!crs.next()) {
                        QueryBuilder qb2 = new QueryBuilder(CommandType.INSERT);
                        qb2.addTable(tableName)
                                .addColumn(thisColumn, "")
                                .addColumn(otherColumn, "")
                                .addValue(idService.getObjectId(object))
                                .addValue(idService.getObjectId(otherObj));
                        executor.execute(qb2.build());
                    }
                }
            }
        }catch (IllegalAccessException | SQLException e){
            e.printStackTrace();
        }
    }


}
