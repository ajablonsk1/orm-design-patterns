package orm;


import orm.schema.SchemaCreator;
import orm.session.Session;
import orm.session.SessionFactory;
import orm.test.*;

import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) throws Exception {
        Session session = SessionFactory.getInstance().createSession();
        Teacher teacher = (Teacher) session.load(Teacher.class,1);
        System.out.println(teacher.subject.subjectName);
        System.out.println(teacher.firstName);
        System.out.println(teacher.lastName);


//        Account account = new Account();
//        account.setUserName("misiek2000");
//        account.setPassword("super-tajne-haslo");
//
//        Student student = new Student();
//        student.setFirstName("Michał");
//        student.setLastName("Studencki");
//
//        student.setAccount(account);
//        account.setStudent(student);
//
//        Professor professor = new Professor();
//        professor.setFirstName("Jan");
//        professor.setLastName("Profesorski");
//
//        professor.addStudent(student);
//        student.addProfessor(professor);
//
//        session.save(student);
//        session.save(professor);
//        session.save(account);
//        session.flush();

    }
}
