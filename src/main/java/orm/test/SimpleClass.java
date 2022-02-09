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

    @ManyToMany(tableName = "tabelaLacznikowa")
    public List<ManyToManyCl> scs;

    @OneToOne(foreignKeyInThisTable = false)
    public OneToOneCl oneToOne;

    @OneToMany
    public List<OneToManyCl> oneToMany;


}
