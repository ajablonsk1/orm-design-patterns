package orm.test;

import orm.annotations.Column;
import orm.annotations.Entity;
import orm.annotations.Id;

@Entity
public class Test {
    @Id
    public int field1;

    @Column
    public String field2;

    public Object field3;
}
