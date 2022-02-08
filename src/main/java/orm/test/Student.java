package orm.test;

import orm.annotations.*;

import java.util.ArrayList;
import java.util.List;

@Entity
public class Student{
    @Id public Integer id;

    @Column public String firstName;
    @Column public String lastName;
    @OneToOne(foreignKeyInThisTable = true) public Account account;
    @OneToMany public List<Grade> grades;


    LoadTest loadTestForPrimitive;

    public Student() {
    }
    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public LoadTest getLoadTestForPrimitive() {
        return loadTestForPrimitive;
    }

    public void setLoadTestForPrimitive(LoadTest loadTestForPrimitive) {
        this.loadTestForPrimitive = loadTestForPrimitive;
    }
}
