package orm.test;

import orm.annotations.Column;
import orm.annotations.Entity;
import orm.annotations.OneToOne;

@Entity
public class TeachingSubject {
    @OneToOne(foreignKeyInThisTable = true)
    Teacher teacher;
    @Column
    public String subjectName;
}
