package orm.test;

import orm.annotations.Column;
import orm.annotations.Entity;
import orm.annotations.ManyToOne;

@Entity
public class Grade {
    @Column public int value;
    @ManyToOne public Student student;
}
