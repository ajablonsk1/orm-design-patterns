package orm.executor;

import javax.sql.RowSet;
import javax.sql.rowset.RowSetFactory;
import javax.sql.rowset.RowSetProvider;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SqlExecutor {
    private final String url;
    private final String user;
    private final String password;
    private final String databaseName;
    private final Boolean urlHasCredentials;


    public SqlExecutor(String url, String databaseName) {
        this.url = url;
        this.user = null;
        this.password = null;
        this.databaseName = databaseName;
        this.urlHasCredentials = false;
        executeUseDatabaseQuery(databaseName);
    }

    public SqlExecutor(String url, String user, String password, String databaseName) {
        this.url = url;
        this.user = user;
        this.password = password;
        this.databaseName = databaseName;
        this.urlHasCredentials = true;

        executeUseDatabaseQuery(databaseName);
    }

    private void executeUseDatabaseQuery(String databaseName) {
        try {
            this.executeSingleQuery(getConnection(), "USE "+ databaseName);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Optional<RowSet>> execute(List<String> queries) {
        List<Optional<RowSet>> results = new ArrayList<>();
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

    public Optional<RowSet> execute(String query) {
        Optional<RowSet> result = Optional.empty();
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

    private Optional<RowSet> executeSingleQuery(Connection conn, String query) throws SQLException {
        Optional<RowSet> result = Optional.empty();
        try (PreparedStatement statement = conn.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS)) {
            boolean hasResults = statement.execute();
            if (hasResults) {
                ResultSet resultSet = statement.getResultSet();
                RowSetFactory rowSetFactory = RowSetProvider.newFactory();
                result = Optional.of(rowSetFactory.createCachedRowSet());
                resultSet.close();
            }
        }
        return result;
    }

    private Connection getConnection() throws SQLException {
        return urlHasCredentials ? DriverManager.getConnection(url, user, password) : DriverManager.getConnection(url);
    }
}
