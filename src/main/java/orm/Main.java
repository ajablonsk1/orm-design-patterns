package orm;


import orm.session.Session;
import orm.session.SessionFactory;
import orm.test.Account;
import orm.test.Student;
import orm.test.Test;

public class Main {
    public static void main(String[] args) throws Exception {
        //Session session = SessionFactory.getInstance("jdbc:mysql://mysql.agh.edu.pl:3306/pholowi1", "pholowi1", "Qo1J2fAHMTSADYdW").createSession();
        Session session = SessionFactory.getInstance("jdbc:mysql://localhost:3306/test", "root", "password").createSession();


        Student student = (Student)session.load(Student.class,2);

        Account account = student.getAccount();

        System.out.println(account.getUserName());

        if (student == account.getStudent()){
            System.out.println("ten sama ref jest ok");
        }else {
            System.out.println("nie jest ok");
        }
    }
}
