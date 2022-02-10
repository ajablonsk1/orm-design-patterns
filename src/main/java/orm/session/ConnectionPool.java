package orm.session;


import orm.utils.Config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ConnectionPool {
    private final Deque<Connection> connections;
    private final HashMap<Thread, Connection> used;
    private final Lock lock = new ReentrantLock(true);
    private final Condition condition = lock.newCondition();
    private boolean closed = false;

    public ConnectionPool() throws Exception {
        Config config = Config.getInstance();
        int size = config.getConnectionPoolSize();
        this.connections = new ArrayDeque<>(size);
        this.used = new HashMap<>(size);
        for (int i = 0; i < size; i++) {
            connections.add(DriverManager.getConnection(
                    config.getDatabaseUrl(),
                    config.getUser(),
                    config.getPassword()));
        }
    }

    public Connection getConnection() throws InterruptedException {
        lock.lock();
        try {
            if (closed) {
                throw new IllegalStateException("ConnectionPool object is closed");
            }
            while (connections.isEmpty()) {
                condition.await();
            }
            Connection conn = connections.removeLast();
            used.put(Thread.currentThread(), conn);
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
            if (used.containsKey(Thread.currentThread())) {
                Connection conn = used.remove(Thread.currentThread());
                connections.add(conn);
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
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
    }
}
