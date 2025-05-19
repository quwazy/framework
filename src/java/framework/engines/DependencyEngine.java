package framework.engines;

import java.util.List;

/**
 * Starts the application.
 * It will create instances of all
 * classes in the engine package.
 * Also, will scan all classes in
 * a project to look for ones
 * with annotations.
 */
public class DependencyEngine {
    private static volatile DependencyEngine instance = null;

    private DependencyEngine() {

    }

    public static DependencyEngine getInstance() {
        if (instance == null) {
            instance = new DependencyEngine();
        }
        return instance;
    }

    protected void creteRepository(List<Class<?>> classes){}

    protected void creteService(List<Class<?>> classes){}

    protected void creteController(List<Class<?>> classes){}

}
