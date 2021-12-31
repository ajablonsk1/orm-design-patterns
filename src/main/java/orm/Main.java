package orm;


import orm.session.Session;
import orm.session.SessionFactory;
import java.util.Set;

public class Main {
    public static void main(String[] args) throws Exception {
        Session session = SessionFactory.getInstance().createSession();
    }
}
