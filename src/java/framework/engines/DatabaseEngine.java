package framework.engines;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class for database operations.
 * This class will store instances of classes
 * annotated with @Entity.
 */
public class DatabaseEngine {
    private static volatile DatabaseEngine instance = null;

    private static Map<String, List<Object>> database;          //class name -> list of entities
    private static Map<String, Object> classRepositoryMap;   //which repository points on which class

    private DatabaseEngine() {
        database = new HashMap<>();
        classRepositoryMap = new HashMap<>();
    }

    public static DatabaseEngine getInstance() {
        if (instance == null) {
            instance = new DatabaseEngine();
        }
        return instance;
    }

    protected void createDatabase(List<Class<?>> classes){
        for (Class<?> cls : classes) {
            String className = cls.getName();
            List<Object> entities = new ArrayList<>();

            database.put(className, entities);
        }
    }

}
