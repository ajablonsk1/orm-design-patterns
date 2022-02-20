package orm.session.finder;

import orm.sql.CommandType;
import orm.sql.Query;
import orm.sql.QueryBuilder;

import java.util.ArrayList;
import java.util.List;

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
        return  new QueryBuilder(CommandType.SELECT).addTable(clazz).addCondition(sql).build();
    }
    public Class<?> getClazz(){
        return clazz;
    }
}
