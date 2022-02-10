package orm.schema;

import orm.annotations.Entity;
import orm.utils.Config;

import java.io.*;
import java.lang.annotation.Annotation;
import java.util.*;
import java.util.stream.Collectors;

public class ClassFinder {
    private final ClassLoader classLoader;
    private final Set<Class<?>> classes;

    public ClassFinder() {
        this.classLoader = ClassLoader.getSystemClassLoader();
        this.classes = new HashSet<>();
    }

    public Set<Class<?>> findEntityClasses() throws Exception {
        if (this.classes.isEmpty()){
            List<String> packageNames = Config.getInstance().getPackages();
            for (String packageName: packageNames){
                this.classes.addAll(findPackageClasses(packageName));
            }
        }
        return this.classes;
    }

    private Set<Class<?>> findPackageClasses(String packageName) throws Exception {
        InputStream stream = classLoader
                .getResourceAsStream(packageName.replaceAll("[.]", "/"));
        if (stream == null){
            throw new Exception("Package "+packageName+" not found");
        }
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        return reader.lines()
                .filter(line -> line.endsWith(".class"))
                .map(line -> getClass(line, packageName))
                .filter(Objects::nonNull)
                .filter(this::isEntityClass)
                .collect(Collectors.toSet());
    }

    private Class<?> getClass(String className, String packageName) {
        try {
            return Class.forName(packageName + "."
                    + className.substring(0, className.lastIndexOf('.')));
        } catch (ClassNotFoundException e) {
            // handle the exception
        }
        return null;
    }

    private boolean isEntityClass(Class<?> cl) {
        Annotation[] annotations = cl.getAnnotationsByType(Entity.class);
        return annotations.length != 0;
    }

}
