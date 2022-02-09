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
        OneToManyCl oneToManyCl = new OneToManyCl();
        OneToOneCl oneToOneCl = new OneToOneCl();

        sc.oneToMany = oneToManyCl;
        oneToManyCl.sc = sc;

        sc.oneToOne = oneToOneCl;
        oneToOneCl.sc =sc;

        session.save(sc);
        session.save(oneToManyCl);
        session.save(oneToOneCl);

        session.flush();



    }
}
