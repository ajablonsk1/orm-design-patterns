package orm.schema;

import orm.annotations.ManyToMany;
import orm.annotations.OneToOne;
import orm.session.ConnectionPool;
import orm.session.Executor;
import orm.sql.CommandType;
import orm.sql.Query;
import orm.sql.QueryBuilder;
import orm.utils.Config;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SchemaCreator {
    private final List<Query> queries = new ArrayList<>();
    private final Executor executor;
    private final ClassFinder finder = new ClassFinder();
    private final ClassScanner scanner = new ClassScanner();
    private Set<Class<?>> entityClasses;

    private final Set<String> associationTables = new HashSet<>();

    public SchemaCreator(ConnectionPool connectionPool) {
        this.executor = new Executor(connectionPool);
    }

    public void createSchema() throws Exception {
        entityClasses = finder.findEntityClasses();
        dropAllTables();
        addQueryForIdTable();
        addQueriesToCreateTables();
        addQueriesForOneToOnes();
        //przy One-to-Many mamy fk w innej tabeli, więc nie potrzeba zapytań
        addQueriesForManyToOnes();
        addQueriesForManyToManys();

        // przy strategii Class Table Inheritance nie potrzeba tu dodatkowego kodu do obsługi dziedziczenia
        executor.execute(queries);
    }

    public void dropAllTables() throws Exception {
        queries.add(new QueryBuilder(CommandType.SET)
                .setForeignKeyChecks(0).build());
        for(Class<?> clazz: entityClasses){
            QueryBuilder qb1 = new QueryBuilder(CommandType.DROP);
            for (Field field: scanner.getManyToManyFields(clazz)){
                QueryBuilder qb2 = new QueryBuilder(CommandType.DROP);
                String associationTable = field.getAnnotation(ManyToMany.class).tableName();
                qb2.addTable(associationTable);
                queries.add(qb2.build());
            }
            qb1.addTable(clazz);
            queries.add(qb1.build());
        }
        queries.add(new QueryBuilder(CommandType.DROP).addTable("id").build());
        queries.add(new QueryBuilder(CommandType.SET)
                .setForeignKeyChecks(1).build());
    }

    public void addQueryForIdTable(){
        queries.addAll(List.of(
                new QueryBuilder(CommandType.CREATE).addTable("id").addColumn("id", "INT").build(),
                new QueryBuilder(CommandType.INSERT).addTable("id").addColumn("id", null).addValue(1).build()
        ));
    }

    public void addQueriesToCreateTables(){
        for (Class<?> clazz: entityClasses){
            List<Field> columns = scanner.getColumns(clazz);
            QueryBuilder qb = new QueryBuilder(CommandType.CREATE).addTable(clazz);
            qb.addId();
            columns.forEach(qb::addColumn);
            queries.add(qb.build());
        }
    }
    public void addQueriesForOneToOnes(){
        for (Class<?> clazz: entityClasses){
            QueryBuilder qb = new QueryBuilder(CommandType.ALTER).addTable(clazz);
            List<Field> fields = scanner.getOneToOneFields(clazz)
                    .stream()
                    .filter(f -> f.getAnnotation(OneToOne.class).foreignKeyInThisTable())
                    .toList();
            if (fields.size() > 0) {
                fields.forEach(qb::addForeignKey);
                queries.add(qb.build());
            }
        }
    }
    public void addQueriesForManyToOnes(){
        for (Class<?> clazz: entityClasses){
            QueryBuilder qb = new QueryBuilder(CommandType.ALTER).addTable(clazz);
            List<Field> fields = scanner.getManyToOneFields(clazz);
            if (fields.size() > 0) {
                fields.forEach(qb::addForeignKey);
                queries.add(qb.build());
            }
        }
    }
    public void addQueriesForManyToManys(){
        for (Class<?> clazz: entityClasses){
            for (Field field: scanner.getManyToManyFields(clazz)){
                String associationTable = field.getAnnotation(ManyToMany.class).tableName();
                QueryBuilder qb = new QueryBuilder();
                if (! associationTables.contains(associationTable)){
                    qb.setCommandType(CommandType.CREATE);
                    associationTables.add(associationTable);
                } else{
                    qb.setCommandType(CommandType.ALTER);
                }
                qb.addTable(associationTable);

                String refTableName = ((Class<?>) ((ParameterizedType) field.getGenericType())
                                            .getActualTypeArguments()[0]).getSimpleName().toLowerCase();
                String columnName = refTableName + "_id";

                qb.addForeignKey(columnName, refTableName);

                queries.add(qb.build());
            }
        }

    }

}
