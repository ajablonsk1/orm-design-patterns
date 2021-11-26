package orm.sql;

import java.util.List;

public abstract class SqlBuilder {

    public SqlBuilder addStatementToQuery(StringBuilder sql,
                                      List<String> list,
                                      String statementBeginning,
                                      String statementSeparation){
        boolean first = true;
        for(String string: list){
            if(first){
                sql.append(statementBeginning);
            }
            else{
                sql.append(statementSeparation);
            }
            sql.append(string);
            first = false;
        }
        return this;
    }
}
