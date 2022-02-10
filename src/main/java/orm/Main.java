package orm;


import orm.annotations.ManyToMany;
import orm.annotations.ManyToOne;
import orm.schema.SchemaCreator;
import orm.session.Session;
import orm.session.SessionFactory;
import orm.session.operations.IdService;
import orm.test.*;

import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) throws Exception {
        Session session = SessionFactory.getInstance().createSession();

        SimpleClass sc1 = new SimpleClass();
        SimpleClass sc2 = new SimpleClass();
        SimpleClass sc3 = new SimpleClass();

        ManyToManyCl mtm1 = new ManyToManyCl();
        ManyToManyCl mtm2 = new ManyToManyCl();
        ManyToManyCl mtm3 = new ManyToManyCl();

        sc1.mtm.add(mtm1);
        mtm1.scs.add(sc1);

        sc2.mtm.add(mtm1);
        mtm1.scs.add(sc2);

        List.of(sc1, sc2, sc3, mtm1, mtm2, mtm3).forEach(session::save);
        session.flush();

        System.out.println("sc1 = " + (new IdService()).getObjectId(sc1));
        System.out.println("sc2 = " + (new IdService()).getObjectId(sc2));
        System.out.println("sc3 = " + (new IdService()).getObjectId(sc3));
        System.out.println("mtm1 = " + (new IdService()).getObjectId(mtm1));
        System.out.println("mtm2 = " + (new IdService()).getObjectId(mtm2));
        System.out.println("mtm3 = " + (new IdService()).getObjectId(mtm3));

        sc1.mtm.add(mtm3);
        mtm3.scs.add(sc1);

        sc1.mtm.remove(mtm1);
        mtm1.scs.remove(sc1);

        List.of(sc1, mtm1, mtm3).forEach(session::update);
        session.flush();
    }
}
