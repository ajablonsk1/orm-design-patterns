package orm.test;

import orm.annotations.Column;
import orm.annotations.Entity;
import orm.annotations.OneToOne;

@Entity
public class Account {
    @Column String userName;
    @Column String password;
    @OneToOne(foreignKeyInThisTable = false) Student student;
}
