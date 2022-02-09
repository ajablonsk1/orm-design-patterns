package orm;


import orm.annotations.ManyToOne;
import orm.schema.SchemaCreator;
import orm.session.Session;
import orm.session.SessionFactory;
import orm.test.*;

import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) throws Exception {
        Session session = SessionFactory.getInstance().createSession();

        SimpleClass sc = new SimpleClass();

        ManyToManyCl mtm = new ManyToManyCl();

        sc.scs = new ArrayList<>(List.of(mtm));

        mtm.scs = new ArrayList<>(List.of(sc));

        session.save(sc);
        session.save(mtm);

        session.flush();


    }
}
