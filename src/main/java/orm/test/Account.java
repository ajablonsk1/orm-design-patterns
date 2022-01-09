package orm.test;

import orm.annotations.Column;
import orm.annotations.Entity;
import orm.annotations.OneToOne;

@Entity
public class Account {
    @Column String userName;
    @Column String password;
    @OneToOne(foreignKeyInThisTable = false) Student student;

    public Account() {

    }

    public Student getStudent() {
        return student;
    }

    public void setStudent(Student student) {
        this.student = student;
    }

    public String getUserName() {
        return userName;
    }
}
