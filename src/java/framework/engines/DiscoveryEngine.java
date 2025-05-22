package framework.engines;

import framework.annotations.components.Component;
import framework.annotations.components.Controller;
import framework.annotations.components.Repository;
import framework.annotations.components.Service;
import framework.annotations.databases.Entity;
import framework.exceptions.FrameworkException;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

public class DiscoveryEngine {
    private static volatile DiscoveryEngine instance = null;
    private static final String PACKAGE_LOCATION = "src/java/";
    private static final String PACKAGE_NAME = "playground";

    protected static List<Class<?>> classes;            //all classes in a project
    protected static List<Class<?>> entityClasses;      //classes annotated with @Entity
    protected static List<Class<?>> repositoryClasses;  //classes annotated with @Repository
    protected static List<Class<?>> serviceClasses;     //classes annotated with @Service
    protected static List<Class<?>> controllerClasses;  //classes annotated with @Controller
    protected static List<Class<?>> componentClasses;   //classes annotated with @Component

    private DiscoveryEngine() throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        classes = new ArrayList<>();
        entityClasses = new ArrayList<>();
        repositoryClasses = new ArrayList<>();
        serviceClasses = new ArrayList<>();
        controllerClasses = new ArrayList<>();
        componentClasses = new ArrayList<>();

        initClasses();
        initDatabase();
        initDependency();
    }

    public static DiscoveryEngine getInstance() throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        if (instance == null) {
            instance = new DiscoveryEngine();
        }
        return instance;
    }

    private static void initClasses() {
        File directory = new File(PACKAGE_LOCATION + PACKAGE_NAME );

        if (directory.exists()) {
            scanDirectory(directory, PACKAGE_NAME);
        } else {
            throw new FrameworkException( "Package path not found: " + directory.getAbsolutePath());
        }
    }

    private static void scanDirectory(File directory, String packageName) {
        File[] files = directory.listFiles();
        if (files == null)
            return;

        for (File file : files) {
            if (file.isDirectory()) {
                scanDirectory(file, packageName + "." + file.getName());
            } else if (file.getName().endsWith(".java")) {
                String className = file.getName().substring(0, file.getName().length() - 5); // Remove .java

                try {
                    Class<?> clazz = Class.forName(packageName + "." + className);
                    classes.add(clazz);

                    if (clazz.isAnnotationPresent(Entity.class)){
                        entityClasses.add(clazz);
                        continue;
                    }
                    if (clazz.isAnnotationPresent(Repository.class)){
                        repositoryClasses.add(clazz);
                        continue;
                    }
                    if (clazz.isAnnotationPresent(Service.class)){
                        serviceClasses.add(clazz);
                        continue;
                    }
                    if (clazz.isAnnotationPresent(Controller.class)){
                        controllerClasses.add(clazz);
                        continue;
                    }
                    if (clazz.isAnnotationPresent(Component.class)){
                        componentClasses.add(clazz);
                        continue;
                    }
                } catch (ClassNotFoundException e) {
                    throw new FrameworkException("Class not found: " + packageName + "." + className);
                }
            }
        }
    }

    private static void initDatabase(){
        DatabaseEngine.getInstance().createDatabase(entityClasses);
        DatabaseEngine.getInstance().mapRepositoryToEntity(repositoryClasses);
    }

    private static void initDependency() throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        DependencyEngine.getInstance().creteService(serviceClasses);
        DependencyEngine.getInstance().creteController(controllerClasses);
        DependencyEngine.getInstance().createComponents(componentClasses);
    }
}
