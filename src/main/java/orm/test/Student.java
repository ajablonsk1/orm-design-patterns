package orm.test;

import orm.annotations.Column;
import orm.annotations.Entity;
import orm.annotations.OneToMany;
import orm.annotations.OneToOne;

import java.util.List;

@Entity
public class Student{
    @Column String firstName;
    @Column String lastName;
    @OneToOne(foreignKeyInThisTable = true) Account account;
    @OneToMany List<Grade> grades;
}
