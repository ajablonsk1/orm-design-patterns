package orm.test;

import orm.annotations.*;
import orm.annotations.OneToOne;

import java.util.List;

@Entity
public class SimpleClass {
    @Id
    public int id;

    @Column
    public String cos;

    @OneToOne(foreignKeyInThisTable = false)
    public OneToOneCl oneToOne;

    @OneToMany
    public List<OneToManyCl> oneToMany;


}
