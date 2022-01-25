package orm.test;

import orm.annotations.Column;
import orm.annotations.Entity;
import orm.annotations.OneToOne;

@Entity
public class Account {
    @Column
    public String userName;
    @Column
    public String password;
    @OneToOne(foreignKeyInThisTable = false)
    public Student student;

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

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
