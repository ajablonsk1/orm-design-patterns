package orm;


import orm.annotations.ManyToMany;
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

        List<SimpleClass> scs = new ArrayList<>();
        List<ManyToManyCl> mtm = new ArrayList<>();

        for (int i = 0; i < 10; i++){
            scs.add(new SimpleClass());
            session.save(scs.get(i));
        }

        for (int i = 0; i < 10; i++){
            mtm.add(new ManyToManyCl());
            session.save(mtm.get(i));
        }

        for (int i = 0; i < 10; i++){
            scs.get(i).scs = mtm;
        }

        for (int i = 0; i < 10; i++){
            mtm.get(i).scs = scs;
        }

        session.flush();
    }
}
