package orm.schema;

import orm.annotations.ManyToMany;
import orm.annotations.OneToOne;
import orm.schema.columns.ForeignKeyColumn;
import orm.schema.columns.PrimaryKeyColumn;
import orm.schema.columns.SimpleColumn;
import orm.schema.reflect.ClassFinder;
import orm.schema.reflect.ClassScanner;
import orm.session.operations.AssociationTableService;
import orm.utils.SqlTypes;
import orm.utils.Utils;

import java.lang.reflect.Field;
import java.util.*;

public class SourceSchemaLoader {
    public static Set<Class<?>> entityClasses;

    public static Schema loadSourceSchema() throws Exception {
        Schema schema = new Schema();
        schema.setIdTableExists(true);

        entityClasses = new ClassFinder().findEntityClasses();
        var scanner = new ClassScanner();
        for (Class<?> clazz : entityClasses){
            Set<SimpleColumn> columns = schema.addTable(clazz);
            String classLowercase = clazz.getSimpleName().toLowerCase();
            if (scanner.getIdField(clazz) != null){
                addIdColumn(columns);
            }
            for (Field field: scanner.getColumns(clazz)){
                addSimpleColumn(columns, field);
            }
            for (Field field: scanner.getOneToOneFields(clazz)){
                if (field.getAnnotation(OneToOne.class).foreignKeyInThisTable()){
                    addForeignKeyColumn(columns, classLowercase, field);
                }
            }
            for (Field field: scanner.getManyToOneFields(clazz)){
                addForeignKeyColumn(columns, classLowercase, field);
            }
            
            for (Field field: scanner.getManyToManyFields(clazz)){
                String tableName = field.getAnnotation(ManyToMany.class).tableName();
                if (! schema.getTables().containsKey(tableName)){
                    schema.getTables().put(tableName, new HashSet<>());
                }
                addColumnToAssociationTable(schema, field, tableName);
            }
        }
        return schema;
    }

    private static void addIdColumn(Set<SimpleColumn> columns){
        columns.add(new PrimaryKeyColumn());
    }

    private static void addColumnToAssociationTable(Schema schema, Field field, String associationTableName) {
        schema.getTables()
                .get(associationTableName)
                .add(new ForeignKeyColumn(
                        AssociationTableService.getColumnNameForField(field),
                        associationTableName,
                        Utils.getInsideClassNameLowercase(field)));
    }

    private static void addSimpleColumn(Set<SimpleColumn> columns, Field field) {
        columns.add(new SimpleColumn(field.getName().toLowerCase(), SqlTypes.getType(field.getType())));
    }

    private static void addForeignKeyColumn(Set<SimpleColumn> columns, String table, Field field) {
        columns.add(new ForeignKeyColumn(field.getName().toLowerCase() + "_id", table, field.getType().getSimpleName().toLowerCase()));
    }


}
