package orm.sql;
import orm.session.Executor;

import javax.sql.rowset.CachedRowSet;
import java.sql.SQLException;
import java.util.Optional;

public class IdGiver {

    private Executor executor;

    public IdGiver(String url, String user, String password, String databaseName){
        this.executor = new Executor(url, user, password, databaseName);
    }

    public int getId() throws SQLException {
        QueryBuilder queryBuilder = new QueryBuilder(CommandType.SELECT);
        Query query = queryBuilder.addColumn("id", "")
                .addTable("id")
                .build();
        Optional<CachedRowSet> crs = executor.execute(query);
        if(crs.isPresent()){
            int id = crs.get().getInt(1);
            updateId(id);
            return id;
        }
        else{
            throw new IllegalStateException("Cannot take id record");
        }
    }

    private void updateId(Integer id) {
        QueryBuilder queryBuilder = new QueryBuilder(CommandType.UPDATE);
        Query query = queryBuilder.addTable("id")
                .setColumn("id", id+1)
                .build();
        executor.execute(query);
    }
}
