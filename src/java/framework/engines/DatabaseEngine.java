package framework.engines;

import framework.annotations.components.Repository;
import framework.annotations.databases.Id;
import framework.exceptions.FrameworkException;
import framework.interfaces.FrameworkRepository;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DatabaseEngine {
    private static volatile DatabaseEngine instance = null;

    private static Long idCounter = 1L;                             //id counter
    private static Map<Class<?>, List<Object>> database;            //class -> list of entities
    private static Map<String, String> repositoryToEntityMap;       //repository name -> class name

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
                database.put(cls, new ArrayList<Object>());
            }else {
                throw new FrameworkException("Class: " + cls.getName() + " does not have @Id annotation");
            }
        }
    }

    protected void mapRepositoryToEntity(List<Class<?>> classes) {
        for (Class<?> cls : classes) {
            if (FrameworkRepository.class.isAssignableFrom(cls) && cls.isAnnotationPresent(Repository.class)){
                repositoryToEntityMap.put(cls.getName(), cls.getAnnotation(Repository.class).entity().getName());
            }else{
                throw new FrameworkException("Class: " + cls.getName() + " does not implement FrameworkRepository interface or does not have @Repository annotation");
            }
        }
    }

    /**
     * Assigning primary key value for new entities
     * @return next Long value for Id filed
     */
    private Long getNextId(){
        return idCounter++;
    }

    /**
     * Saves an object in a database, based on
     * JSON fields. Creates object, maps it's
     * fields and insert values into fields
     * @param jsonMap from http request
     */
    public void addEntity(HashMap<String, String> jsonMap) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        String className = getEntityName(jsonMap);

        if (className == null){
            throw new FrameworkException("No entity found for given JSON map");
        }

        Class<?> cls = Class.forName(className);
        Object obj = cls.getDeclaredConstructor().newInstance();   //create a new object

        for (Field field : cls.getDeclaredFields()) {
            if (field.isAnnotationPresent(Id.class)) {      //insert new ID value
                field.setAccessible(true);
                field.set(obj, getNextId());
            } else {
                field.setAccessible(true);
                String value = jsonMap.get(field.getName());
                if (value != null) {
                    Object convertedValue = convertValue(field.getType(), value);
                    field.set(obj, convertedValue);
                }
            }
        }

        database.get(cls).add(obj);     // Save to a database
    }

    /**
     * For given JSON map checks which entity in
     * a database has same fields as given JSON man
     * @param jsonMap from http request
     * @return class name of entity in a database
     */
    private String getEntityName(HashMap<String, String> jsonMap){
        for (Class<?> cls : database.keySet()){
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

                if (counter == 0){                  //if every field in a map has a pair in class, we found an exact class
                    return cls.getName();
                }
            }
        }

        return null;
    }

    /**
     * Converting data from JSON to
     * match with the object's field data type
     * @param type data type object needs
     * @param value from JSON
     * @return converted value
     */
    private Object convertValue(Class<?> type, String value) {
        if (type == String.class) {
            return value;
        } else if (type == int.class || type == Integer.class) {
            return Integer.parseInt(value);
        } else if (type == long.class || type == Long.class) {
            return Long.parseLong(value);
        } else if (type == boolean.class || type == Boolean.class) {
            return Boolean.parseBoolean(value);
        } else if (type == double.class || type == Double.class) {
            return Double.parseDouble(value);
        } else if (type == float.class || type == Float.class) {
            return Float.parseFloat(value);
        }
        throw new FrameworkException("Unsupported field type: " + type);
    }
}
