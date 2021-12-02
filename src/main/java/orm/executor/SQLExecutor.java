package orm.executor;

import javax.sql.rowset.CachedRowSet;
import javax.sql.rowset.RowSetFactory;
import javax.sql.rowset.RowSetProvider;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SQLExecutor {
    private final String url;
    private final String user;
    private final String password;
    private final Boolean urlHasCredentials;

    public SQLExecutor(String url) {
        this.url = url;
        this.user = null;
        this.password = null;
        this.urlHasCredentials = false;
    }

    public SQLExecutor(String url, String user, String password) {
        this.url = url;
        this.user = user;
        this.password = password;
        this.urlHasCredentials = true;
    }

    public List<Optional<CachedRowSet>> execute(List<String> queries) {
        List<Optional<CachedRowSet>> results = new ArrayList<>();
        try (Connection conn = getConnection()){
            conn.setAutoCommit(false);
            for (String query : queries) {
                results.add(executeSingleQuery(conn, query));
            }
            conn.commit();
        } catch (SQLException e) {
            // TODO: obsługa błędu
            e.printStackTrace();
        }
        return results;
    }

    public Optional<CachedRowSet> execute(String query) {
        Optional<CachedRowSet> result = Optional.empty();
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            result = executeSingleQuery(conn, query);
            conn.commit();
        } catch (SQLException e) {
            // TODO: obsługa błędu
            e.printStackTrace();
        }
        return result;
    }

    private Optional<CachedRowSet> executeSingleQuery(Connection conn, String query) throws SQLException {
        Optional<CachedRowSet> result = Optional.empty();
        try (PreparedStatement statement = conn.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS)) {
            boolean hasResults = statement.execute();
            if (hasResults) {
                CachedRowSet rowSet = cacheAndClose(statement.getResultSet());
                result = Optional.of(rowSet);
            } else if (statement.getUpdateCount() != 0){
                CachedRowSet rowSet = cacheAndClose(statement.getGeneratedKeys());
                result = Optional.of(rowSet);
            }
        }
        return result;
    }

    private Connection getConnection() throws SQLException {
        return urlHasCredentials ? DriverManager.getConnection(url, user, password) : DriverManager.getConnection(url);
    }

    private CachedRowSet cacheAndClose(ResultSet resultSet) throws SQLException {
        RowSetFactory rowSetFactory = RowSetProvider.newFactory();
        CachedRowSet rowSet = rowSetFactory.createCachedRowSet();
        rowSet.populate(resultSet);
        resultSet.close();
        return rowSet;
    }
}
