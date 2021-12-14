package orm.scanner;

import orm.annotations.Entity;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class ClassFinder {
    private final ClassLoader classLoader;
    private Set<Class> classes;

    public ClassFinder() {
        this.classLoader = ClassLoader.getSystemClassLoader();
        this.classes = new HashSet<>();
    }

    public Set<Class> loadClasses() {
        String[] paths = System.getProperty("java.class.path").split(System.getProperty("path.separator"));
        File[] files = new File[paths.length];
        for (int i = 0; i < paths.length; i++) {
            files[i] = new File(paths[i]);
        }
        visitFiles(files);
        return this.classes;
    }

    private void visitFiles(File[] files) {
        if(files == null) {
            throw new IllegalArgumentException();
        }
        for (File file : files) {
            if (file.isDirectory()) {
                visitFiles(file.listFiles());
            } else {
                String name = file.getName();
                if (name.endsWith(".jar")) {
                    processJarFile(file);
                } else if (name.endsWith(".class")) {
                    processClassFile(file);
                }
            }
        }
    }

    private void processJarFile(File file) {
        try {
            JarFile jarFile = new JarFile(file);
            Iterator<JarEntry> iterator = jarFile.entries().asIterator();

            URL[] urls = {new URL("jar:file:" + file.getAbsolutePath() + "!/")};
            URLClassLoader urlClassLoader = URLClassLoader.newInstance(urls);

            while (iterator.hasNext()) {
                JarEntry entry = iterator.next();
                String name = entry.getName();
                if (name.endsWith(".class")) {
                    String className = name.substring(0, name.lastIndexOf(".class")).replace('/', '.');
                    Class cl = urlClassLoader.loadClass(className);
                    if (isAnnotated(cl))
                        this.classes.add(cl);
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void processClassFile(File file) {
        try {
            String path = file.getAbsolutePath();
            for (String classPath : System.getProperty("java.class.path").split(System.getProperty("path.separator"))) {
                if (path.startsWith(classPath)) {
                    path = path.substring(classPath.length() + 1, path.length());
                    break;
                }
            }
            String className = path.substring(0, path.lastIndexOf(".class")).replace('/', '.');

            Class cl = classLoader.loadClass(className);
            if (isAnnotated(cl))
                this.classes.add(cl);

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    // TODO: Uwzględnić pozostałe adnotacje
    private boolean isAnnotated(Class cl) {
        Annotation[] annotations = cl.getAnnotationsByType(Entity.class);
        return annotations.length != 0;
    }
}
