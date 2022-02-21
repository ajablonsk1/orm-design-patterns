package orm.session.finder;

import orm.schema.reflect.ClassFinder;
import orm.schema.reflect.ClassScanner;
import orm.sql.CommandType;
import orm.sql.Query;
import orm.sql.QueryBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class QueryObject {
    private final Class<?> clazz;
    String sql;

    public QueryObject(Class<?> clazz) {
        this.clazz = clazz;
    }

    public void setCriteria(Criteria criteria1){
        sql = criteria1.generateSql();
    }
    public Query build(){
        String baseTable = clazz.getSimpleName().toLowerCase();

        List<Class<?>> classes = new ClassScanner().getParentEntityClasses(clazz);
        List<String> joinTables = classes.stream().map(clazz -> clazz.getSimpleName().toLowerCase()).toList();
        List<String> joins = joinTables.stream()
                .map(table -> String
                        .format("JOIN %s ON %s.id = %s.id",
                                table, table, baseTable)).toList();

        QueryBuilder qb = new QueryBuilder(CommandType.SELECT);
        qb.addColumn(baseTable+".id");

        String tableStr = baseTable + " " + String.join(" ", joins);

        qb.addTable(tableStr);
        qb.addCondition(sql).build();

        return qb.build();
    }
    public Class<?> getClazz(){
        return clazz;
    }
}
