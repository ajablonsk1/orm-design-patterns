package orm.session;

import orm.annotations.ManyToMany;
import orm.annotations.OneToOne;
import orm.schema.ClassFinder;
import orm.schema.ClassScanner;
import orm.sql.CommandType;
import orm.sql.IdGiver;
import orm.sql.Query;
import orm.sql.QueryBuilder;

import java.io.FileReader;
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
                    qb.addValue(getObjectId(field.get(object)));
                }
            }

            for (Field field : classScanner.getManyToOneFields(clazz)) {
                String[] a = field.getType().toString().split("\\.");
                String columnName = a[a.length - 1].toLowerCase() + "_id";
                qb.addColumn(columnName, null);
                qb.addValue(getObjectId(field.get(object)));
            }

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

    private void updateRecord(Class<?> cl, Object object) {
        try {
            QueryBuilder qb = new QueryBuilder(CommandType.UPDATE);
            qb.addTable(cl);

            for (Field column : classScanner.getColumns(cl)) {
                qb.addColumn(column)
                        .addValue(column.get(object));
            }


            executor.execute(qb.build());

            // update Many-to-Many

            for (Field field : classScanner.getManyToManyFields(cl)){
                String tableName = field.getAnnotation(ManyToMany.class).tableName();

                // TODO wyciągnąć do zewnętrznej metody i sprawdzić czy działa w teście jednostkowym
                Class<?> otherClass = (Class<?>) ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];

                String thisColumnName = classScanner.getManyToManyFields(otherClass)
                                            .stream()
                                            .filter(f -> f.getAnnotation(ManyToMany.class).tableName() == tableName)
                                            .reduce((first, second) -> first)
                                            .get()
                                            .getName().toLowerCase() + "_id";

                String otherColumnName = field.getName().toLowerCase() + "_id";

                // select from association table
                QueryBuilder qb2 = new QueryBuilder(CommandType.SELECT);
                qb2.addTable(tableName)
                        .addColumn(thisColumnName, "")
                        .addColumn(otherColumnName, "");

                // find records to insert

                // find records to delete
            }


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
        for(Object object: objectsToDelete){

        }

        // TODO
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
