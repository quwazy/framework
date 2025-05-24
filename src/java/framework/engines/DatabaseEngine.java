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

/**
 * Creates a database for all classes annotated with @Entity annotation.
 * Maps Repository to it's entity class name.
 * Inserts new entities into database.
 * Removes entities from database.
 * Gets all entities from database.
 * Gets entity by id from database.
 * Creates an object from JSON map.
 */
public class DatabaseEngine {
    private static volatile DatabaseEngine instance = null;
    private static Long idCounter = 1L;

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

    /**
     * Creating a database for all classes annotated with @Entity annotation.
     * @param classes List of classes annotated with @Entity annotation.
     */
    protected void createDatabase(List<Class<?>> classes) {
        for (Class<?> cls : classes) {
            boolean found = false;
            for (Field field : cls.getDeclaredFields()){
                if (field.isAnnotationPresent(Id.class)) {
                    if (field.getType() != Long.class){
                        throw new FrameworkException("Field: " + field.getName() + " of class: " + cls.getName() + " must be of type Long");
                    }
                    database.put(cls, new ArrayList<>());
                    found = true;
                    break;
                }
            }
            if (!found){
                throw new FrameworkException("Class: " + cls.getName() + " does not have @Id annotation");
            }
        }
    }

    /**
     * Mapping Repository to it's entity class name.
     * @param classes List of classes annotated with @Repository annotation.
     */
    protected void mapRepositoryToEntity(List<Class<?>> classes) {
        for (Class<?> cls : classes) {
            if (FrameworkRepository.class.isAssignableFrom(cls) && cls.isAnnotationPresent(Repository.class)){
                repositoryToEntityMap.put(cls.getName(), cls.getAnnotation(Repository.class).entity().getName());
            }
            else{
                throw new FrameworkException("Class: " + cls.getName() + " with @Repository annotation does not implement FrameworkRepository interface.");
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
     * GET one entity from the database table by id
     * @param repositoryName name of repository.
     * @param id of entity in the database table.
     * @return Object of entity with given id.
     */
    public Object getEntityById(String repositoryName, Long id) throws ClassNotFoundException, IllegalAccessException {
        Class<?> clazz = Class.forName(repositoryToEntityMap.get(repositoryName));
        List<Object> objectList = database.get(clazz);

        for (Object obj : objectList) {
            for (Field field : clazz.getDeclaredFields()) {
                if (field.isAnnotationPresent(Id.class)) {
                    field.setAccessible(true);
                    Object fieldValue = field.get(obj);

                    if (fieldValue.equals(id)) {
                        return obj;
                    }
                }
            }
        }

        return null;
    }

    /**
     * GET all entities from the database table.
     * @param repositoryName name of repository.
     */
    public List<Object> getAllEntities(String repositoryName) throws ClassNotFoundException {
        return database.get(Class.forName(repositoryToEntityMap.get(repositoryName)));
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
     * DELETE entity from the database table by id param.
     * @param repositoryName name of repository.
     * @param id of entity in the database table.
     */
    public void deleteEntity(String repositoryName, Long id) throws ClassNotFoundException, IllegalAccessException {
        Class<?> clazz = Class.forName(repositoryToEntityMap.get(repositoryName));
        List<Object> objectList = database.get(clazz);

        for (Object obj : objectList) {
            for (Field field : clazz.getDeclaredFields()) {
                if (field.isAnnotationPresent(Id.class)) {
                    field.setAccessible(true);
                    Object fieldValue = field.get(obj);

                    if (fieldValue.equals(id)) {
                        database.get(clazz).remove(obj);
                    }
                }
            }
        }
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
