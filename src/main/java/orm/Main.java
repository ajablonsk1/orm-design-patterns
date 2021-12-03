package orm;

import orm.scanner.ClassFinder;
import orm.scanner.ClassScanner;
import orm.session.Session;
import orm.session.SessionFactory;
import java.util.Set;

public class Main {
    public static void main(String[] args){
        Session session = SessionFactory.getInstance().createSession();
    }
}
