package orm.sql;
import orm.session.ConnectionPool;
import orm.session.Executor;

import javax.sql.rowset.CachedRowSet;
import java.sql.SQLException;
import java.util.Optional;

public class IdGiver {

    private Executor executor;

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
