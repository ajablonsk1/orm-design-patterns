package orm.session;

import orm.executor.SqlExecutor;
import orm.scanner.ClassFinder;
import orm.scanner.ClassScanner;

import java.util.Set;

public class Session {
    // TODO: przekazywanie parametrów połączenia do bazy przez konstruktor klasy Session
    private SqlExecutor executor;
    // wiem że się nie hardcoduje credentiali w kodzie, ale na razie w tej bazie nic nie ma
    // później usunę te credentiale - Piotrek

    // konstruktor package-private
    // Ważne, aby tylko klasa SessionFactory mogła tworzyć obiekty klasy Session
    Session() {
        this.executor = new SqlExecutor("jdbc:mysql://mysql.agh.edu.pl:3306", "pmakare1", "kmjUCKSydDxdZ022", "pmakare1");



    }

}
