package orm.test;

import orm.annotations.Column;
import orm.annotations.Entity;
import orm.annotations.Id;

@Entity
public class InheritingClass extends SimpleClass {
    @Id
    public int id;
    @Column
    public String name;
}
