package orm.test;

import orm.annotations.Column;
import orm.annotations.Entity;
import orm.annotations.Id;
import orm.annotations.OneToOne;

@Entity
public class LoadTest {
    @Id
    int id;
    @OneToOne(foreignKeyInThisTable = true) Student student;

    public Student getStudent() {
        return student;
    }

    public void setStudent(Student student) {
        this.student = student;
    }

    public LoadTest(){

    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
