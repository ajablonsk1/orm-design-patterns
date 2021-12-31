package orm;

import orm.schema.ClassFinder;
import orm.schema.ClassScanner;

import java.util.Set;

public class Main {
    public static void main(String[] args) throws Exception {
        ClassFinder classFinder = new ClassFinder();
        Set<Class> annotatedClasses = classFinder.findEntityClasses();
        for (Class cl : annotatedClasses) {
            System.out.println(cl.getName());
        }
        ClassScanner classScanner = new ClassScanner();
        classScanner.scanClasses(annotatedClasses);
    }
}
