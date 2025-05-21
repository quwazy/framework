package framework.engines;

import framework.annotations.components.Repository;
import framework.annotations.databases.Id;
import framework.exceptions.FrameworkException;
import framework.interfaces.FrameworkRepository;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DatabaseEngine {
    private static volatile DatabaseEngine instance = null;

    private static volatile Long idCounter = 1L;
    private static Map<String, List<Object>> database;          //class name -> list of entities
    private static List<Class<?>> entityClasses;
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

    public Long getNextId(){
        return idCounter++;
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

        entityClasses = classes;
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

    //stize mi hash mapa sa JSON poljima
    //i sad ja na osnovu polja treba da provalim koji mi entitet tacno treba
    //prolazim kroz sve klase sa anotacijom entitet
    //prolazim kroz sva njihova polja i ignorisem polje ID
    //ako su sva polja ista, to je taj entitet
    public void addEntity(HashMap<String, String> jsonMap) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        String className = getEntityName(jsonMap);
        if (className == null){
            throw new FrameworkException("No entity found for given JSON map");
        }

        Object obj = Class.forName(className).getDeclaredConstructor().newInstance();

        List<Object> entities = database.get(className);

    }

    /**
     * For given JSON map checks which entity in
     * a database has same fields as given JSON man
     * @param jsonMap from http request
     * @return class name of entity in a database
     */
    private String getEntityName(HashMap<String, String> jsonMap){
        for (Class<?> cls : entityClasses){
            if (jsonMap.size() == cls.getDeclaredFields().length - 1){  //ignore @ID field
                int counter = jsonMap.size();
                for (String jsonFieldName : jsonMap.keySet()){
                    for (Field field : cls.getDeclaredFields()){
                        if (field.isAnnotationPresent(Id.class)){
                            continue;
                        }
                        if (field.getName().equals(jsonFieldName)){
                            counter--;
                        }
                    }
                }

                //if every field in a map has a pair in class, we found exact class
                if (counter == 0){
                    System.out.println("NASAO");
                    return cls.getName();
                }
            }
        }

        return null;
    }
}
