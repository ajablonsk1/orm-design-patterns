package orm.session;

import orm.sql.CommandType;
import orm.sql.Query;
import orm.sql.QueryBuilder;

import javax.sql.rowset.CachedRowSet;
import java.sql.SQLException;

public class IdGiver {

    private final Executor executor;

    public IdGiver(ConnectionPool connectionPool){
        this.executor = new Executor(connectionPool);
    }

    public int getId() throws SQLException {
        QueryBuilder queryBuilder = new QueryBuilder(CommandType.SELECT);
        Query query = queryBuilder.addColumn("id", "")
                .addTable("id")
                .build();
        CachedRowSet crs = executor.execute(query)
                .orElseThrow(SQLException::new);
        crs.next();
        int id = crs.getInt(1);
        updateId(id);
        return id;
    }

    private void updateId(Integer id) {
        QueryBuilder queryBuilder = new QueryBuilder(CommandType.UPDATE);
        Query query = queryBuilder.addTable("id")
                .setColumn("id", id+1)
                .build();
        executor.execute(query);
    }
}
