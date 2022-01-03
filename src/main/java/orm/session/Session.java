package orm.session;

import orm.session.Executor;
import orm.schema.ClassFinder;
import orm.schema.ClassScanner;

import java.util.Set;

public class Session {
    private Executor executor;
    // konstruktor package-private, aby tylko klasa SessionFactory mogła tworzyć obiekty klasy Session
    Session() throws Exception {
        //TODO: parametry połączenia przez plik konfiguracyjny
        this.executor = new Executor("jdbc:mysql://mysql.agh.edu.pl:3306", "pmakare1", "kmjUCKSydDxdZ022", "pmakare1");
        ClassFinder classFinder = new ClassFinder();
        ClassScanner classScanner = new ClassScanner();
    }
}
