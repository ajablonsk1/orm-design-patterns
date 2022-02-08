package orm.test;

import orm.annotations.Column;
import orm.annotations.Entity;
import orm.annotations.OneToOne;
@Entity
public class Teacher extends Person{
    @OneToOne(foreignKeyInThisTable = false)
    public TeachingSubject subject;
}
