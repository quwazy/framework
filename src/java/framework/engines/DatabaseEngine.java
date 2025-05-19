package framework.engines;

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
    private static Map<String, List<Object>> database;
    //mapiranje repozitorijuma sa entitetima

    private DatabaseEngine() {
        database = new HashMap<>();
    }

    public static DatabaseEngine getInstance() {
        if (instance == null) {
            instance = new DatabaseEngine();
        }
        return instance;
    }

    protected void createDatabase(List<Class<?>> classes){

    }
}
