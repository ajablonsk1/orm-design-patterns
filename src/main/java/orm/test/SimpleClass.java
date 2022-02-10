package orm.test;

import orm.annotations.*;
import orm.annotations.OneToOne;

import java.util.ArrayList;
import java.util.List;

@Entity
public class SimpleClass {
    @Id
    public int id;
    @Column
    public String cos;

    @ManyToMany(tableName = "tabelaLacznikowa")
    public List<ManyToManyCl> mtm = new ArrayList<>();

    @OneToOne(foreignKeyInThisTable = false)
    public OneToOneCl oneToOne;

    @OneToOne(foreignKeyInThisTable = true)
    public OneToOneCl2 oneToOne2;

    @OneToMany
    public List<OneToManyCl> oneToMany;


}
