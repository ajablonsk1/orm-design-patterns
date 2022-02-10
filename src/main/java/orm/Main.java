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

//        Post post1 = new Post();
//        post1.name = "pierwszy post";
//        Post post2 = new Post();
//        Tag tag1 = new Tag();
//        Tag tag2 = new Tag();
//        Tag tag3 = new Tag();
//        post1.tags.add(tag1);
//        post1.tags.add(tag2);
//        post2.tags.add(tag2);
//        post2.tags.add(tag3);
//        tag1.posts.add(post1);
//        tag2.posts.add(post1);
//        tag2.posts.add(post2);
//        tag3.posts.add(post2);
//
//        session.save(post1);
//        session.save(post2);
//        session.save(tag1);
//        session.save(tag2);
//        session.save(tag3);
//
//        session.flush();
//
//        Post post = (Post) session.load(Post.class,5);
//        Tag tag = (Tag)session.load(Tag.class,1);
//        System.out.println(tag.posts.get(0).id);


        InheritingClass sc1 = new InheritingClass();
        sc1.cos = "sc1";
        InheritingClass sc2 = new InheritingClass();
        sc2.cos = "sc1";
        ManyToManyCl manyToManyCl1 = new ManyToManyCl();
        ManyToManyCl manyToManyCl2 = new ManyToManyCl();
        sc1.mtm.add(manyToManyCl1);
        sc1.mtm.add(manyToManyCl2);
        sc2.mtm.add(manyToManyCl1);
        sc2.mtm.add(manyToManyCl2);
        manyToManyCl1.scs.add(sc1);
        manyToManyCl1.scs.add(sc2);
        manyToManyCl2.scs.add(sc1);
        manyToManyCl2.scs.add(sc2);
        OneToOneCl oneToOneCl1 = new OneToOneCl();
        OneToOneCl oneToOneCl2 = new OneToOneCl();
        sc1.oneToOne = oneToOneCl1;
        sc2.oneToOne = oneToOneCl2;
        oneToOneCl1.sc = sc1;
        oneToOneCl2.sc = sc2;
        OneToManyCl oneToManyCl11 = new OneToManyCl();
        OneToManyCl oneToManyCl12 = new OneToManyCl();
        OneToManyCl oneToManyCl21 = new OneToManyCl();
        OneToManyCl oneToManyCl22 = new OneToManyCl();
        sc1.oneToMany.add(oneToManyCl11);
        sc1.oneToMany.add(oneToManyCl12);
        sc2.oneToMany.add(oneToManyCl21);
        sc2.oneToMany.add(oneToManyCl22);
        oneToManyCl11.sc = sc1;
        oneToManyCl12.sc = sc1;
        oneToManyCl21.sc = sc2;
        oneToManyCl22.sc = sc2;
        OneToOneCl2 oneToOneCl221 = new OneToOneCl2();
        OneToOneCl2 oneToOneCl222 = new OneToOneCl2();
        sc1.name = "name1";
        sc2.name = "name2";
        sc1.oneToOneCl2 = oneToOneCl221;
        sc2.oneToOneCl2 = oneToOneCl222;
        oneToOneCl221.sc = sc1;
        oneToOneCl222.sc = sc2;

        session.save(sc1);
        session.save(sc2);
        session.save(manyToManyCl1);
        session.save(manyToManyCl2);
        session.save(oneToOneCl1);
        session.save(oneToOneCl2);
        session.save(oneToManyCl11);
        session.save(oneToManyCl12);
        session.save(oneToManyCl21);
        session.save(oneToManyCl22);
        session.save(oneToOneCl221);
        session.save(oneToOneCl222);
        session.flush();
        session.delete(sc1);
        session.delete(sc2);
        session.delete(manyToManyCl1);
        session.delete(manyToManyCl2);
        session.delete(oneToOneCl1);
        session.delete(oneToOneCl2);
        session.delete(oneToManyCl11);
        session.delete(oneToManyCl12);
        session.delete(oneToManyCl21);
        session.delete(oneToManyCl22);
        session.delete(oneToOneCl221);
        session.delete(oneToOneCl222);
        session.flush();
    }
}
