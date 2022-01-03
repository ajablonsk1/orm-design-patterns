package orm.session;

import orm.session.Executor;
import orm.schema.ClassFinder;
import orm.schema.ClassScanner;

import java.util.Set;

public class Session {
    private Executor executor;
    // wiem że się nie hardcoduje credentiali w kodzie, ale na razie w tej bazie nic nie ma
    // później usunę te credentiale - Piotrek

    // konstruktor package-private
    // Ważne, aby tylko klasa SessionFactory mogła tworzyć obiekty klasy Session
    Session() throws Exception {
        //TODO: parametry połączenia przez plik konfiguracyjny
        this.executor = new Executor("jdbc:mysql://mysql.agh.edu.pl:3306", "pmakare1", "kmjUCKSydDxdZ022");
        //this.executor = new Executor("jdbc:mysql://localhost:3306");
        ClassFinder classFinder = new ClassFinder();
        ClassScanner classScanner = new ClassScanner();
        //classScanner.scanClasses(classFinder.findEntityClasses());
    }
}
