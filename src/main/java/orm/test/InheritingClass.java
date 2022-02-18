package orm.test;

import orm.annotations.Column;
import orm.annotations.Entity;
import orm.annotations.Id;
import orm.annotations.OneToOne;

@Entity
public class InheritingClass extends SimpleClass {
    @Id
    public int id;

}
