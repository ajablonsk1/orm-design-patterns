package orm.session;

import orm.schema.SchemaCreator;
import orm.utils.Config;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SessionFactory {
    private volatile static SessionFactory sessionFactory;
    private List<Session> sessions = new ArrayList<>();
    private ConnectionPool connectionPool;
    //private ThreadLocal<Session> session;

    private SessionFactory () {
        try {
            this.connectionPool = new ConnectionPool();
            SchemaCreator schemaCreator = new SchemaCreator(connectionPool);
            if (Config.getInstance().isCreateSchemaOnStart())
                schemaCreator.createSchema();
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
        //TODO: threadLocal
        Session session = new Session(connectionPool);
        sessions.add(session);
        return session;
    }
}
