package framework.engines;

import framework.annotations.components.Repository;
import framework.annotations.databases.Entity;
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
                if (!cls.getAnnotation(Repository.class).entity().isAnnotationPresent(Entity.class)){
                    throw new FrameworkException("Class name you provided in @Repository annotation: " + cls.getAnnotation(Repository.class).entity().getName() + " doesn't have @Entity annotation");
                }
                repositoryToEntityMap.put(cls.getName(), cls.getAnnotation(Repository.class).entity().getName());
            }
            else{
                throw new FrameworkException("Class: " + cls.getName() + " with @Repository annotation does not implement FrameworkRepository interface.");
            }
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
     * Saving Object into the database.
     * Chceks if provided entity can be inserted in the repository.
     * @param repositoryName Repository name to insert the entity into the database.
     * @param entity object to be inserted into a database.
     */
    public void insertEntity(String repositoryName, Object entity) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        String entityName = repositoryToEntityMap.get(repositoryName);
        if (entityName == null){
            throw new FrameworkException("Repository: " + repositoryName + " is not working with Entity you provided");
        }

        Class<?> clazz = Class.forName(entityName);
        Object obj = clazz.getDeclaredConstructor().newInstance();

        for (Field field : clazz.getDeclaredFields()) {
            field.setAccessible(true);

            if (field.isAnnotationPresent(Id.class)) {
                Long generatedId = getNextId();
                field.set(obj, generatedId);
            }
            else {
                Field sourceField;
                try {
                    sourceField = entity.getClass().getDeclaredField(field.getName());
                    sourceField.setAccessible(true);
                    field.set(obj, sourceField.get(entity));
                } catch (NoSuchFieldException e) {
                    throw new FrameworkException("No such filed in entity");
                }
            }
        }

        database.get(clazz).add(obj);       //inserting into database
        //TODO
        for (Class<?> cls : database.keySet()){
            List<Object> objectList = database.get(cls);
            objectList.forEach(System.out::println);
        }
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
     * Creating an entity object from JSON map.
     * @param className Name of the entity class.
     * @param jsonMap from request.
     * @return Object to be inserted into a database.
     */
    protected Object createEntity(String className, HashMap<String, String> jsonMap) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        Class<?> cls = Class.forName(className);
        if (!checkPostParams(cls, jsonMap)){
            throw new FrameworkException("JSON map does not match with given class: " + className);
        }
        if (!database.containsKey(cls)){
            throw new FrameworkException("Class: " + className + " does not have @Entity annotation");
        }

        Object obj = cls.getDeclaredConstructor().newInstance();

        for (Field field : cls.getDeclaredFields()) {
            if (!field.isAnnotationPresent(Id.class)) {
                field.setAccessible(true);
                String value = jsonMap.get(field.getName());
                if (value != null) {
                    field.set(obj, convertValue(field.getType(), value));   //set object's field value
                }
                else {
                    field.set(obj, null);
                }
            }
        }

        return obj;
    }

    /**
     * Checks if the class's object we want to create and JSON map,
     * have the same attributes.
     * @param cls class name.
     * @param jsonMap map from request.
     * @return true if JSON map matches with class fields, false otherwise.
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
