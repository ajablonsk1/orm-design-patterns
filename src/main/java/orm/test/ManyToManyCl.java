package orm.test;

import orm.annotations.Entity;
import orm.annotations.Id;
import orm.annotations.ManyToMany;

import java.util.List;

@Entity
public class ManyToManyCl {
    @Id
    public int id;

    @ManyToMany(tableName = "tabelaLacznikowa")
    public List<SimpleClass> scs;
}
