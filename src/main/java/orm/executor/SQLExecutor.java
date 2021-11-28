package orm.executor;

import javax.sql.RowSet;
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

    public List<Optional<RowSet>> executeUnit(List<String> unitOfWork) {
        List<Optional<RowSet>> results = new ArrayList<>();
        try (Connection conn = urlHasCredentials ? DriverManager.getConnection(url, user, password) : DriverManager.getConnection(url)) {
            conn.setAutoCommit(false);
            for (String query : unitOfWork) {
                results.add(execute(conn, query));
            }
            conn.commit();
        } catch (SQLException e) {
            // TODO: obsługa błędu
            e.printStackTrace();
        }
        return results;
    }

    public Optional<RowSet> execute(Connection conn, String query) {
        Optional<RowSet> result = null;
        try (PreparedStatement statement = conn.prepareStatement(query)) {
            boolean hasResults = statement.execute();
            if (hasResults) {
                ResultSet resultSet = statement.getResultSet();
                RowSetFactory rowSetFactory = RowSetProvider.newFactory();
                result = Optional.of(rowSetFactory.createCachedRowSet());
                resultSet.close();
            } else {
                result = Optional.empty();
            }
        } catch (SQLException e) {
            // TODO: obsługa błędu
            e.printStackTrace();
        }
        return result;
    }
}
