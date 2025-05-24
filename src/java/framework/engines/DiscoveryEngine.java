package framework.engines;

import framework.annotations.components.Component;
import framework.annotations.components.Controller;
import framework.annotations.components.Repository;
import framework.annotations.components.Service;
import framework.annotations.databases.Entity;
import framework.exceptions.FrameworkException;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Scans directory which uses a framework.
 * Looks for classes with annotations defined
 * in the framework.annotation package,
 * sorts them by lists,
 * and starts the initialization of
 * DatabaseEngine & DependencyEngine.
 */
public class DiscoveryEngine {
    private static volatile DiscoveryEngine instance = null;
    private static final String PACKAGE_LOCATION = "src/java/";
    private static final String PACKAGE_NAME = "playground";

    private static List<Class<?>> entityClassesList;      //classes annotated with @Entity
    private static List<Class<?>> repositoryClassesList;  //classes annotated with @Repository
    private static List<Class<?>> serviceClassesList;     //classes annotated with @Service
    private static List<Class<?>> controllerClassesList;  //classes annotated with @Controller
    private static List<Class<?>> componentClassesList;   //classes annotated with @Component

    private DiscoveryEngine() throws Exception {
        entityClassesList = new ArrayList<>();
        repositoryClassesList = new ArrayList<>();
        serviceClassesList = new ArrayList<>();
        controllerClassesList = new ArrayList<>();
        componentClassesList = new ArrayList<>();

        initClasses();
        initDatabase();
        initDependency();
    }

    public static void getInstance() throws Exception {
        if (instance == null) {
            instance = new DiscoveryEngine();
        }
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
            }
            else if (file.getName().endsWith(".java")) {
                String className = file.getName().substring(0, file.getName().length() - 5);

                try {
                    Class<?> clazz = Class.forName(packageName + "." + className);

                    if (clazz.isAnnotationPresent(Entity.class)){
                        entityClassesList.add(clazz);
                        continue;
                    }
                    if (clazz.isAnnotationPresent(Repository.class)){
                        repositoryClassesList.add(clazz);
                        continue;
                    }
                    if (clazz.isAnnotationPresent(Service.class)){
                        serviceClassesList.add(clazz);
                        continue;
                    }
                    if (clazz.isAnnotationPresent(Controller.class)){
                        controllerClassesList.add(clazz);
                        continue;
                    }
                    if (clazz.isAnnotationPresent(Component.class)){
                        componentClassesList.add(clazz);
                        continue;
                    }
                }
                catch (ClassNotFoundException e) {
                    throw new FrameworkException("Class not found: " + packageName + "." + className);
                }
            }
        }
    }

    private static void initDatabase() {
        DatabaseEngine.getInstance().createDatabase(entityClassesList);
        DatabaseEngine.getInstance().mapRepositoryToEntity(repositoryClassesList);
    }

    private static void initDependency() throws Exception {
        DependencyEngine.getInstance().creteService(serviceClassesList);
        DependencyEngine.getInstance().creteController(controllerClassesList);
        DependencyEngine.getInstance().createComponents(componentClassesList);
    }
}
