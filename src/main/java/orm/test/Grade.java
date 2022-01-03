package orm.test;

import orm.annotations.Column;
import orm.annotations.Entity;
import orm.annotations.ManyToOne;

@Entity
public class Grade {
    @Column int value;
    @ManyToOne Student student;
}
