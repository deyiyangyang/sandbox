import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

public class ClassScanner {

    public static void main(String[] args) {
        ClassScanner cs = new ClassScanner();
        Set<Class<?>> classes = cs.scanClasses(cs.getClass().getClassLoader());
        classes.forEach(System.out::println);
    }

    public Set<Class<?>> scanClasses(ClassLoader cl) {

        Predicate<Path> isRegularFile = Files::isRegularFile;
        Predicate<Path> isClassFile = a -> a.getFileName().toString().endsWith(".class");
        Predicate<Path> predicate = isRegularFile.and(isClassFile);

        Function<Path, String> toString = Path::toString;
        UnaryOperator<String> toClassName = a -> a.replace('/', '.').substring(0,
                a.length() - ".class".length());
        Function<String, Class<?>> loadClass = a -> {
            try {
                return cl.loadClass(a);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        };
        Function<Path, Class<?>> mapper = toString.andThen(toClassName).andThen(loadClass);

        Set<Class<?>> classes = new HashSet<>();
        try {
            Enumeration<URL> resources = cl.getResources("");
            while (resources.hasMoreElements()) {
                URL resource = resources.nextElement();
                Path root = Paths.get(resource.toURI());
                Files.walk(root).filter(predicate).map(root::relativize).map(mapper)
                        .forEach(classes::add);
            }
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
        return classes;
    }
}
