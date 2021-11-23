package orm;

import orm.scanner.ClassFinder;

import java.util.Set;

public class Main {
    public static void main(String[] args){
        ClassFinder classFinder = new ClassFinder();
        Set<Class> annotatedClasses = classFinder.loadClasses();
        for (Class cl : annotatedClasses) {
            System.out.println(cl.getName());
        }
    }
}
