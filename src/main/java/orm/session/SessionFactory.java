package orm.session;

import orm.schema.SchemaCreator;
import orm.utils.Config;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SessionFactory {
    private volatile static SessionFactory sessionFactory;
    private ConnectionPool connectionPool;
    private ThreadLocal<Session> session;

    private SessionFactory () {
        try {
            this.connectionPool = new ConnectionPool();
            SchemaCreator schemaCreator = new SchemaCreator(connectionPool);
            if (Config.getInstance().isCreateSchemaOnStart())
                schemaCreator.createSchema();
            session = ThreadLocal.withInitial(() -> null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static SessionFactory getInstance(){
        // lazy-loading, double null-check, thread-safe Singleton
        if (sessionFactory == null){
            synchronized (SessionFactory.class){
                if (sessionFactory == null){
                    sessionFactory = new SessionFactory();
                }
            }
        }
        return sessionFactory;
    }

    public Session createSession() throws Exception {
        if (session.get() == null) {
            synchronized (this) {
                if (session.get() == null) {
                    session.set(new Session(connectionPool));
                }
            }
        }
        return session.get();
    }
}
