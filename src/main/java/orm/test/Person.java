package orm.test;

import orm.annotations.Column;
import orm.annotations.Entity;

@Entity
public class Person {
    @Column
    public String firstName;
    @Column
    public String lastName;

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
}
