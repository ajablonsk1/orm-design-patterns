package orm.session;


import orm.utils.Config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ConnectionPool {
    private final int size;
    private final Connection[] connections;
    private boolean[] used;
    private final Lock lock = new ReentrantLock(true);
    private final Condition condition = lock.newCondition();
    private int lastFree = 0;
    private int lastUsed = 0;
    private boolean closed = false;

    public ConnectionPool() throws Exception {
        Config config = Config.getInstance();
        this.size = config.getConnectionPoolSize();
        this.connections = new Connection[size];
        this.used = new boolean[size];
        for (int i = 0; i < size; i++) {
            connections[i] = DriverManager.getConnection(
                    config.getDatabaseUrl(),
                    config.getUser(),
                    config.getPassword());
            used[i] = false;
        }
    }

    public Connection getConnection() throws InterruptedException {
        lock.lock();
        try {
            if (closed) {
                throw new IllegalStateException("ConnectionPool object is closed");
            }
            while (used[lastFree]) {
                condition.await();
            }
            Connection conn = connections[lastFree];
            used[lastFree] = true;
            lastFree = (lastFree + 1) % size;
            return conn;
        } finally {
            lock.unlock();
        }
    }

    public void releaseConnection() {
        lock.lock();
        try {
            if (closed) {
                throw new IllegalStateException("ConnectionPool object is closed");
            }
            if (used[lastUsed]) {
                used[lastUsed] = false;
                lastUsed = (lastUsed + 1) % size;
                condition.signal();
            }
        } finally {
            lock.unlock();
        }
    }

    public void close() {
        lock.lock();
        try {
            closed = true;
            for (Connection conn : connections) {
                conn.close();
            }
        } catch (SQLException e) {
            // TODO: Obsługa błędu
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
    }
}
