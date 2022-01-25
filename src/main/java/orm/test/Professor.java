package orm.test;

import orm.annotations.Entity;
import orm.annotations.ManyToMany;

import java.util.ArrayList;
import java.util.List;

@Entity
public class Professor extends Person{
    @ManyToMany(tableName = "professorstudents", columnName = "student_id")
    public List<Student> students = new ArrayList<>();
    public Professor(){

    }

    public void addStudent(Student e){
        students.add(e);
    }

    public String getFirstName() {
        return super.getFirstName();
    }

    public void setFirstName(String firstName) {
        super.setFirstName(firstName);
    }

    public String getLastName() {
        return super.getLastName();
    }

    public void setLastName(String lastName) {
        super.setLastName(lastName);
    }
}
