package orm.schema;

import orm.annotations.ManyToMany;
import orm.schema.columns.ForeignKeyColumn;
import orm.schema.columns.PrimaryKeyColumn;
import orm.schema.columns.SimpleColumn;
import orm.schema.reflect.ClassFinder;
import orm.schema.reflect.ClassScanner;
import orm.session.ConnectionPool;
import orm.session.Executor;
import orm.sql.CommandType;
import orm.sql.Query;
import orm.sql.QueryBuilder;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SchemaUpdater {
    private final List<Query> queries = new ArrayList<>();
    private final Executor executor;
    private final ClassFinder finder = new ClassFinder();
    private final ClassScanner scanner = new ClassScanner();
    private Set<Class<?>> entityClasses;


    private Set<String> tablesToKeep;
    private Set<String> tablesToDrop;
    private Set<String> tablesToAdd;

    private Schema dbSchema;
    private Schema srcSchema;

    private final Set<String> associationTables = new HashSet<>();

    public SchemaUpdater(ConnectionPool connectionPool) {
        this.executor = new Executor(connectionPool);
    }

    public void updateSchema() throws Exception {
        dbSchema = DatabaseSchemaLoader.loadDatabaseSchema(executor);
        srcSchema = SourceSchemaLoader.loadSourceSchema();

        tablesToDrop = dbSchema.getTables().keySet().stream()
                .filter(t -> ! srcSchema.getTables().containsKey(t))
                .collect(Collectors.toSet());

        tablesToKeep = dbSchema.getTables().keySet().stream()
                .filter(t -> srcSchema.getTables().containsKey(t))
                .collect(Collectors.toSet());

        tablesToAdd = srcSchema.getTables().keySet().stream()
                .filter(t -> ! dbSchema.getTables().containsKey(t))
                .collect(Collectors.toSet());

        if (! dbSchema.isIdTableExists()){
            addQueryForIdTable();
        }

        addQueriesToDropAssociationTables();
        addQueriesToDropForeignKeys();
        addQueriesToDropClassTables();
        addQueriesToDropSimpleColumns();
        addQueriesToCreateClassTables();
        addQueriesToAddForeignKeys();
        addQueriesToAddSimpleColumns();
        addQueriesToCreateAssociationTables();


        executor.execute(queries);
    }

    private void addQueriesToCreateAssociationTables() {
        for (String table: tablesToAdd){
            if (! hasIdColumn(srcSchema, table)){
                QueryBuilder qb = new QueryBuilder(CommandType.CREATE);
                qb.addTable(table);
                for (SimpleColumn column: srcSchema.getTables().get(table)){
                    if (column instanceof ForeignKeyColumn fkColumn){
                        qb.addColumn(column.getName(), "INT");
                    }
                }
                queries.add(qb.build());
            }
        }

        for (String table: tablesToAdd){
            if (! hasIdColumn(srcSchema, table)){
                for (SimpleColumn column: srcSchema.getTables().get(table)){
                    if (column instanceof ForeignKeyColumn fkColumn){
                        QueryBuilder qb = new QueryBuilder(CommandType.ALTER);
                        qb.addTable(table);
                        qb.addForeignKeyConstraint(column.getName(), ((ForeignKeyColumn) column).getRefTable(), ((ForeignKeyColumn) column).getFkConstraintName());
                        queries.add(qb.build());
                    }
                }
            }
        }
    }

    private void addQueriesToDropSimpleColumns() {
        for (String table: tablesToKeep){
            for (SimpleColumn column : dbSchema.getTables().get(table)) {
                if (! srcSchema.getTables().get(table).contains(column)) {
                    QueryBuilder qb = new QueryBuilder(CommandType.ALTER);
                    qb.addTable(table).setDropColumn(column.getName());
                    queries.add(qb.build());
                }
            }
        }
    }

    private void addQueriesToDropAssociationTables() {
        List<String> associationTablesToDrop = tablesToDrop.stream()
                .filter(t -> dbSchema.getTables().get(t).stream().allMatch(c -> c instanceof ForeignKeyColumn)).toList();
        for (String table: associationTablesToDrop){
            QueryBuilder qb = new QueryBuilder(CommandType.DROP).addTable(table);
            queries.add(qb.build());
            tablesToDrop.remove(table);
        }
    }

    private void addQueriesToDropForeignKeys() {
        for (String table: tablesToDrop){
            for (SimpleColumn column : dbSchema.getTables().get(table)){
                if (column instanceof ForeignKeyColumn fkColumn){
                    dropFkConstraintAndColumn(table, fkColumn);
                }
            }
        }
        for (String table: tablesToKeep){
            for (SimpleColumn column : dbSchema.getTables().get(table)) {
                if (column instanceof ForeignKeyColumn fkColumn && ! srcSchema.getTables().get(table).contains(column)) {
                    dropFkConstraintAndColumn(table, fkColumn);
                }
            }
        }
    }

    private void addQueriesToDropClassTables(){
        for (String table: tablesToDrop){
            queries.add(new QueryBuilder(CommandType.DROP).addTable(table).build());
        }
    }

    private void dropFkConstraintAndColumn(String table, ForeignKeyColumn fkColumn) {
        QueryBuilder qb = new QueryBuilder(CommandType.ALTER)
                .addTable(table)
                .setDropForeignKey(fkColumn.getFkConstraintName());
        queries.add(qb.build());
        QueryBuilder qb2 = new QueryBuilder(CommandType.ALTER)
                .addTable(table)
                .setDropColumn(fkColumn.getName());
        queries.add(qb2.build());
    }

    private void addQueryForIdTable(){
        queries.addAll(List.of(
                new QueryBuilder(CommandType.CREATE).addTable("id").addColumn("id", "INT").build(),
                new QueryBuilder(CommandType.INSERT).addTable("id").addColumn("id").addValue(1).build()
        ));
    }
    private void addQueriesToCreateClassTables(){
        for (String table: tablesToAdd){
            if (hasIdColumn(srcSchema, table)) {
                QueryBuilder qb = new QueryBuilder(CommandType.CREATE);
                qb.addTable(table);
                for (SimpleColumn column : srcSchema.getTables().get(table)) {
                    if (column instanceof PrimaryKeyColumn){
                        qb.addColumn("id", "INT PRIMARY KEY");
                    }
                    else if (!(column instanceof ForeignKeyColumn)) {
                        qb.addColumn(column.getName(), column.getType());
                    }
                }
                queries.add(qb.build());
            }
        }
    }

    private void addQueriesToAddForeignKeys(){
        for (String table: Stream.concat(tablesToAdd.stream(), tablesToKeep.stream()).toList()){
            if (hasIdColumn(srcSchema, table)) {
                QueryBuilder qb = new QueryBuilder(CommandType.ALTER);
                qb.addTable(table);
                for (SimpleColumn column : srcSchema.getTables().get(table)) {
                    if (column instanceof ForeignKeyColumn fkColumn && (dbSchema.getTables().get(table) == null || ! dbSchema.getTables().get(table).contains(column))) {
                        qb.addColumn(column.getName(), "INT");
                    }
                }
                queries.add(qb.build());

                qb = new QueryBuilder(CommandType.ALTER);
                qb.addTable(table);
                for (SimpleColumn column : srcSchema.getTables().get(table)) {
                    if (column instanceof ForeignKeyColumn fkColumn && (dbSchema.getTables().get(table) == null || ! dbSchema.getTables().get(table).contains(column))) {
                        qb.addForeignKeyConstraint(column.getName(), fkColumn.getRefTable(), fkColumn.getFkConstraintName());
                    }
                }
                queries.add(qb.build());
            }
        }
    }

    private void addQueriesToAddSimpleColumns(){
        for (String table: tablesToKeep){
            QueryBuilder qb = new QueryBuilder(CommandType.ALTER);
            qb.addTable(table);
            for (SimpleColumn column : srcSchema.getTables().get(table)) {
                if (! dbSchema.getTables().get(table).contains(column))
                    qb.addColumn(column.getName(), column.getType());
            }
            queries.add(qb.build());
        }
    }

    private boolean hasIdColumn(Schema schema, String table){
        return schema.getTables().get(table).stream().anyMatch(c -> c.getName().equals("id"));
    }
}
