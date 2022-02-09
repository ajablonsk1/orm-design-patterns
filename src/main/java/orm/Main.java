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
        sc.cos = "@$#@$";

        OneToManyCl oneToManyCl = new OneToManyCl();
        OneToOneCl oneToOneCl = new OneToOneCl();

        sc.oneToMany = new ArrayList<>();
        sc.oneToMany.add(oneToManyCl);
        oneToManyCl.sc = sc;

        sc.oneToOne = oneToOneCl;
        oneToOneCl.sc =sc;

        session.save(sc);
        session.save(oneToManyCl);
        session.save(oneToOneCl);

        OneToManyCl someNewOneToManyCl = new OneToManyCl();
        someNewOneToManyCl.sc = sc;

        session.save(someNewOneToManyCl);

        session.flush();

        SimpleClass sc2 = new SimpleClass();

        sc2.cos = "lubie placki";
        sc2.oneToMany = new ArrayList<>();
        sc2.oneToOne = oneToOneCl;

        oneToOneCl.sc = sc2;

        session.save(sc2);
        session.update(oneToOneCl);

        sc.cos = "Haha, xd";
        sc.oneToMany.add(someNewOneToManyCl);
        session.update(sc);

        session.flush();

        InheritingClass inheritingClass = new InheritingClass();
        inheritingClass.cos = "sgsgs";
        inheritingClass.name = "qweryterw";
        session.save(inheritingClass);
        session.flush();
        session.delete(inheritingClass);
        session.flush();
        session.delete(sc);
        session.delete(sc2);
        session.flush();

    }
}
