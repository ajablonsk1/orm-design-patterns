package orm.session;

import orm.session.Executor;
import orm.schema.ClassFinder;
import orm.schema.ClassScanner;
import orm.sql.CommandType;
import orm.sql.Query;
import orm.sql.QueryBuilder;

import javax.sql.rowset.CachedRowSet;
import java.lang.reflect.Field;
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
        this.executor = new Executor("jdbc:mysql://mysql.agh.edu.pl:3306", "pmakare1", "kmjUCKSydDxdZ022", "pmakare1");
        classFinder = new ClassFinder();
        classScanner = new ClassScanner();
    }

    public void save(Object object) {
        //Nie musze sprawdzac czy obiekt jest w zbiorze, poniewaz jezeli jest to sie nie doda (jako ze mamy HashSet)
        objectsToSave.add(object);
    }

    public Object load(Integer id) {
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
}
