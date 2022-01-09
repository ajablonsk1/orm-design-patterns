package orm;


import orm.session.Session;
import orm.session.SessionFactory;
import orm.test.Account;
import orm.test.Student;

public class Main {
    public static void main(String[] args) throws Exception {
        Session session = SessionFactory.getInstance().createSession();
//        SchemaCreator schemaCreator = new SchemaCreator();
//        schemaCreator.createSchema();


        //
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
