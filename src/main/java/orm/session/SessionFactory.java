package orm.session;

import java.util.ArrayList;
import java.util.List;

public class SessionFactory {
    private SessionFactory () {}
    private volatile static SessionFactory sessionFactory;
    private List<Session> sessions = new ArrayList<>();

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
        Session session = new Session();
        sessions.add(session);
        return session;
    }
}
