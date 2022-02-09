package orm.session;

import orm.sql.Query;

import javax.sql.rowset.CachedRowSet;
import javax.sql.rowset.RowSetFactory;
import javax.sql.rowset.RowSetProvider;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Executor {
    private final ConnectionPool connectionPool;

    public Executor(ConnectionPool connectionPool) {
        this.connectionPool = connectionPool;
    }

    public List<Optional<CachedRowSet>> execute(List<Query> queries) {
        List<Optional<CachedRowSet>> results = new ArrayList<>();
        for (Query query : queries) {
            results.add(executeSingleQuery(query));
        }
        return results;
    }

    public Optional<CachedRowSet> execute(Query query) {
        return executeSingleQuery(query);
    }

    private Optional<CachedRowSet> executeSingleQuery(Query query) {
        Optional<CachedRowSet> result = Optional.empty();
        System.out.println("Executing: "+query);
        Connection conn = getConnection();
        try (PreparedStatement statement = conn.prepareStatement(query.toString(), PreparedStatement.RETURN_GENERATED_KEYS)) {
            List<Object> values = query.getValues();
            for (int i = 0; i < values.size(); i++) {
                statement.setObject(i+1, values.get(i));
            }

            boolean hasResults = statement.execute();
            if (hasResults) {
                CachedRowSet rowSet = cacheAndClose(statement.getResultSet());
                result = Optional.of(rowSet);
            } else if (statement.getUpdateCount() != 0){
                CachedRowSet rowSet = cacheAndClose(statement.getGeneratedKeys());
                result = Optional.of(rowSet);
            }
        } catch (SQLException e) {
            // TODO: Obsługa błędu
            e.printStackTrace();
        } finally {
            releaseConnection();
        }
        return result;
    }

    private Connection getConnection() {
        Connection connection = null;
        try {
            connection = connectionPool.getConnection();
        } catch (InterruptedException e) {
            // TODO: Obsługa błędu
            e.printStackTrace();
        }
        return connection;
    }

    private void releaseConnection() {
        connectionPool.releaseConnection();
    }

    private CachedRowSet cacheAndClose(ResultSet resultSet) throws SQLException {
        RowSetFactory rowSetFactory = RowSetProvider.newFactory();
        CachedRowSet rowSet = rowSetFactory.createCachedRowSet();
        rowSet.populate(resultSet);
        resultSet.close();
        return rowSet;
    }
}
