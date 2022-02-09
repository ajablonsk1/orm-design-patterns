package orm.test;

import orm.annotations.*;
import orm.annotations.OneToOne;

@Entity
public class SimpleClass {
    @Id
    public int id;

    @Column
    public String cos;

    @OneToOne(foreignKeyInThisTable = false)
    public OneToOneCl oneToOne;

    @OneToMany
    public OneToManyCl oneToMany;


}
