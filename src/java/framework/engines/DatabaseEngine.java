package framework.engines;

import framework.annotations.components.Repository;
import framework.annotations.databases.Id;
import framework.exceptions.FrameworkException;
import framework.interfaces.FrameworkRepository;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DatabaseEngine {
    private static volatile DatabaseEngine instance = null;

    private static volatile Long idCounter = 1L;
    private static Map<String, List<Object>> database;          //class name -> list of entities
    private static Map<String, String> repositoryToEntityMap;   //repository name -> class name

    private DatabaseEngine() {
        database = new HashMap<>();
        repositoryToEntityMap = new HashMap<>();
    }

    public static DatabaseEngine getInstance() {
        if (instance == null) {
            instance = new DatabaseEngine();
        }
        return instance;
    }

    protected void createDatabase(List<Class<?>> classes) {
        for (Class<?> cls : classes) {
            boolean foundId = false;

            for (Field field : cls.getDeclaredFields()){
                if (field.isAnnotationPresent(Id.class)) {
                    if (field.getType() != Long.class){
                        throw new FrameworkException("Field: " + field.getName() + " of class: " + cls.getName() + " must be of type Long");
                    }
                    foundId = true;
                    break;
                }
            }

            if (foundId){
                String className = cls.getName();
                List<Object> entities = new ArrayList<>();
                database.put(className, entities);
            }else {
                throw new FrameworkException("Class: " + cls.getName() + " does not have @Id annotation");
            }
        }
    }

    protected void mapRepositoryToEntities(List<Class<?>> classes) {
        for (Class<?> cls : classes) {
            if (FrameworkRepository.class.isAssignableFrom(cls) && cls.isAnnotationPresent(Repository.class)){
                repositoryToEntityMap.put(cls.getName(), cls.getAnnotation(Repository.class).entity().getName());
            }else{
                throw new FrameworkException("Class: " + cls.getName() + " does not implement FrameworkRepository interface or does not have @Repository annotation");
            }
        }
    }

    public Long getNextId(){
        return idCounter++;
    }
}
