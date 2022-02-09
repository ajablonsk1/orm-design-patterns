package orm.test;

import orm.annotations.Entity;
import orm.annotations.Id;
import orm.annotations.ManyToOne;

@Entity
public class OneToManyCl {

    @ManyToOne public SimpleClass sc;

    @Id
    public int id;
}