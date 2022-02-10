package orm.test;

import orm.annotations.Entity;
import orm.annotations.Id;

@Entity
public class OneToOneCl2 {
    @orm.annotations.OneToOne(foreignKeyInThisTable = false)
    public SimpleClass sc;

    @Id
    public int id;
}