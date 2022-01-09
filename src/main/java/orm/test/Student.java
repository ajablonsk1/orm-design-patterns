package orm.test;

import orm.annotations.Column;
import orm.annotations.Entity;
import orm.annotations.OneToOne;

@Entity
public class Student{
    @Column String firstName;
    @Column String lastName;
    @OneToOne(foreignKeyInThisTable = true) Account account;
    @OneToOne(foreignKeyInThisTable = false)
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
