package orm.test;

import orm.annotations.Entity;
import orm.annotations.Id;

@Entity
public class OneToOneCl {
    @orm.annotations.OneToOne(foreignKeyInThisTable = true)
    public SimpleClass sc;

    @Id
    public int id;
}
