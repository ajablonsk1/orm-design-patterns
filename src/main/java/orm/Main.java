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

        Post post1 = new Post();
        post1.name = "pierwszy post";
        Post post2 = new Post();
        Tag tag1 = new Tag();
        Tag tag2 = new Tag();
        Tag tag3 = new Tag();
        post1.tags.add(tag1);
        post1.tags.add(tag2);
        post2.tags.add(tag2);
        post2.tags.add(tag3);
        tag1.posts.add(post1);
        tag2.posts.add(post1);
        tag2.posts.add(post2);
        tag3.posts.add(post2);

        session.save(post1);
        session.save(post2);
        session.save(tag1);
        session.save(tag2);
        session.save(tag3);

        session.flush();

        Post post = (Post) session.load(Post.class,5);
        Tag tag = (Tag)session.load(Tag.class,1);
        System.out.println(tag.posts.get(0).id);






    }
}
