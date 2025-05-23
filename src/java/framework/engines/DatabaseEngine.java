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
    private static Map<Long, Class<?>> idToClassNameMap;              //id in database -> class's name in database

    private DatabaseEngine() {
        database = new HashMap<>();
        repositoryToEntityMap = new HashMap<>();
        idToClassNameMap = new HashMap<>();
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
            }
            else{
                throw new FrameworkException("Class: " + cls.getName() + " does not implement FrameworkRepository interface or does not have @Repository annotation");
            }
        }
    }

    /**
     * Saving Object into the database
     * @param repositoryName
     * @param newEntity
     */
    public void insertEntity(String repositoryName, Object newEntity) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        String entityName = repositoryToEntityMap.get(repositoryName);
        if (entityName == null){
            throw new FrameworkException("Repository: " + repositoryName + " is not working with Entity you provided");
        }

        Class<?> clazz = Class.forName(entityName);
        Object obj = clazz.getDeclaredConstructor().newInstance();
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true); // allow access to private fields

            if (field.isAnnotationPresent(Id.class)) {
                // Assign a new Long ID (you can generate this however you like)
                Long generatedId = getNextId();
                field.set(obj, generatedId);
                idToClassNameMap.put(generatedId, clazz);
            } 
            else {
                // Copy value from original object
                Field sourceField;
                try {
                    sourceField = newEntity.getClass().getDeclaredField(field.getName());
                    sourceField.setAccessible(true);
                    Object value = sourceField.get(newEntity);
                    field.set(obj, value);
                } catch (NoSuchFieldException e) {
                    // Skip if field not found (optional)
                }
            }
        }
        
        database.get(clazz).add(obj);
        //TODO
        for (Class<?> cls : database.keySet()){
            List<Object> objectList = database.get(cls);
            objectList.forEach(System.out::println);
        }
    }

    /**
     * GET all entities from database table
     */
    public List<Object> getEntities(String repositoryName) throws ClassNotFoundException {
        String entityName = repositoryToEntityMap.get(repositoryName);
        if (entityName == null){
            throw new FrameworkException("Repository: " + repositoryName + " is not working with Entity you provided");
        }

        Class<?> clazz = Class.forName(entityName);
        return database.get(clazz);
    }

    public Object getEntityById(String repositoryName, Long id) throws ClassNotFoundException, IllegalAccessException {
        String entityName = repositoryToEntityMap.get(repositoryName);
        if (entityName == null){
            throw new FrameworkException("Repository: " + repositoryName + " is not working with Entity you provided");
        }

        Class<?> clazz =Class.forName(entityName);
        List<Object> objectList = database.get(clazz);

        for (Object obj : objectList) {
            for (Field field : clazz.getDeclaredFields()) {
                if (field.isAnnotationPresent(Id.class)) {
                    field.setAccessible(true); // In case the field is private
                    Object fieldValue = field.get(obj);

                    if (fieldValue instanceof Long && ((Long) fieldValue).equals(id)) {
                        return obj; // Found the match
                    }
                }
            }
        }

        return null;
    }

    /**
     * For given JSON return Object
     */
    protected Object createEntity(String className, HashMap<String, String> jsonMap) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        Class<?> cls = Class.forName(className);
        if (!checkPostParams(cls, jsonMap)){
            throw new FrameworkException("JSON map does not match with given class: " + className);
        }
        if (!database.containsKey(cls)){
            throw new FrameworkException("Class: " + className + " does not have @Entity annotation");
        }

        Object obj = cls.getDeclaredConstructor().newInstance();   //create a new object

        for (Field field : cls.getDeclaredFields()) {
            if (!field.isAnnotationPresent(Id.class)) {
                field.setAccessible(true);
                String value = jsonMap.get(field.getName());
                if (value != null) {
                    Object convertedValue = convertValue(field.getType(), value);
                    field.set(obj, convertedValue);
                }
            }
        }

        return obj;
    }

    /**
     * Da li se POST parametri poklapaju sa Entitetom u kontroller klasi
     */
    private boolean checkPostParams(Class<?> cls, HashMap<String, String> jsonMap) {
        if (jsonMap.size() != cls.getDeclaredFields().length - 1) {
            return false;
        }
        int counter = jsonMap.size();
        for (Field field : cls.getDeclaredFields()) {
            if (field.isAnnotationPresent(Id.class)) {
                continue;
            }
            if (jsonMap.containsKey(field.getName())) {
                counter--;
            }
        }
        return counter == 0;
    }

    /**
     * Assigning primary key value for new entities
     * @return next Long value for Id filed
     */
    private static Long getNextId(){
        return idCounter++;
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
        }
        else if (type == int.class || type == Integer.class) {
            return Integer.parseInt(value);
        }
        else if (type == long.class || type == Long.class) {
            return Long.parseLong(value);
        }
        else if (type == boolean.class || type == Boolean.class) {
            return Boolean.parseBoolean(value);
        }
        else if (type == double.class || type == Double.class) {
            return Double.parseDouble(value);
        }
        else if (type == float.class || type == Float.class) {
            return Float.parseFloat(value);
        }
        throw new FrameworkException("Unsupported field type: " + type);
    }
}
