package us.anvildevelopment.util.tools.database;
import com.fasterxml.jackson.databind.ObjectMapper;
import us.anvildevelopment.util.tools.database.annotations.Key;
import us.anvildevelopment.util.tools.database.annotations.MemoryOnly;
import us.anvildevelopment.util.tools.exceptions.TypeNotSetException;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FileConnector {
    private final String collectionName;
    public final ObjectMapper mapper = new ObjectMapper();
    public final Class<?> type;
    public File root;
    public File storage;

    public FileConnector(String collectionName, Class<?> type, File root) {
        this.collectionName = collectionName;
        this.type = type;
        this.root = root;
        this.storage = new File(root, collectionName);
    }

    public String getFileName(Object object) {
        try {
            for (Field f : object.getClass().getDeclaredFields()) {
                if (f.isAnnotationPresent(Key.class)) {
                    f.setAccessible(true);
                    return mapper.writeValueAsString(f.get(object)) + ".json";
                }
            }
            return System.currentTimeMillis() + ".json"; // Fallback to timestamp
        } catch (Exception e) {
            return System.currentTimeMillis() + ".json";
        }
    }

    public void saveData(Object object, String key, String value) throws IllegalAccessException, IOException {
        checkCollection();
        Path folderPath = Paths.get(storage.getAbsolutePath(), key);
        //File folder = new File(storage, key);
        try {
            // Create all parent directories if they don't exist (idempotent & atomic)
            Files.createDirectories(folderPath);
            System.out.println("[[UTIL1] Created folder: " + folderPath.toAbsolutePath() + " ]");
        } catch (IOException e) {
            System.err.println("[[UTIL1] Failed to create folder: " + folderPath.toAbsolutePath());
            e.printStackTrace();
            // Or rethrow: throw new IOException("Could not create directory: " + folderPath, e);
        }
        //if (!folder.exists()) {
        //    folder.mkdirs();
            //System.out.println("[[UTIL1] Created folder: " + folder.getAbsolutePath()+" ]");
       // }
        File file = new File(folderPath.toFile(), value + ".json");
        System.out.println("[[UTIL1] Writing to file: " + file.getAbsolutePath() + " ]");

        Map<String, Object> data = new HashMap<>();
        data.put(key, value);
        for (Field f : object.getClass().getDeclaredFields()) {
            f.setAccessible(true);
            if (!f.isAnnotationPresent(MemoryOnly.class)) {
                data.put(f.getName(), f.get(object));
            }
        }
        mapper.writeValue(file, data);
    }

    public <T> T getObject(String key, String value) throws IllegalAccessException, IOException, TypeNotSetException {
        if (type == null) throw new TypeNotSetException();
        return (T) getObject(key, value, type);
    }

    public <T> T getObject(String key, String value, Class<T> clazz) throws IllegalAccessException, IOException {
        checkCollection();
        File file = new File(new File(storage, key), value + ".json");
        if (!file.exists()) {
            System.out.println("[[UTIL1] File not found: " + file.getAbsolutePath() + " ]");
            return null;
        }
        System.out.println("[[UTIL1] Reading from file: " + file.getAbsolutePath() + " ]");

        Map<String, Object> data;
        try {
            data = mapper.readValue(file, Map.class);
        } catch (IOException e) {
            System.err.println("[UTIL1!] Failed to read file " + file.getAbsolutePath() + ": " + e.getMessage());
            throw e;
        }

        T instance = null;
        try {
            instance = clazz.newInstance();
            for (Field f : clazz.getDeclaredFields()) {
                f.setAccessible(true);
                if (!f.isAnnotationPresent(MemoryOnly.class)) {
                    Object valueFromFile = data.get(f.getName());
                    if (valueFromFile != null) {
                        //System.out.println("Setting field " + f.getName() + " with value " + valueFromFile);
                        f.set(instance, mapper.readValue(mapper.writeValueAsString(valueFromFile), f.getType()));
                    } else {
                        //System.out.println("No value found for field " + f.getName());
                    }
                }
            }
        } catch (InstantiationException e) {
            System.err.println("[UTIL1!] Failed to instantiate " + clazz.getName() + ": " + e.getMessage());
            throw new IOException("[UTIL1!] Instantiation failed: " + e.getMessage());
        }
        return instance;
    }

    public List<Object> getObjects(Class<?> clazz) {
        List<Object> objects = new ArrayList<>();
        checkCollection();

        File[] folders = storage.listFiles(File::isDirectory);
        if (folders != null) {
            for (File folder : folders) {
                File[] files = folder.listFiles((dir, name) -> name.endsWith(".json"));
                if (files != null) {
                    for (File file : files) {
                        try {
                            Map<String, Object> data = mapper.readValue(file, Map.class);
                            Object instance = clazz.newInstance();
                            for (Field f : clazz.getDeclaredFields()) {
                                f.setAccessible(true);
                                if (!f.isAnnotationPresent(MemoryOnly.class)) {
                                    Object valueFromFile = data.get(f.getName());
                                    if (valueFromFile != null) {
                                        f.set(instance, mapper.readValue(mapper.writeValueAsString(valueFromFile), f.getType()));
                                    }
                                }
                            }
                            objects.add(instance);
                        } catch (IOException | IllegalAccessException | InstantiationException e) {
                            System.err.println("[UTIL1!] Error reading file " + file.getName() + ": " + e.getMessage());
                        }
                    }
                }
            }
        }
        return objects;
    }

    public void checkCollection() {
        if (root == null) {
            root = new File("ObjectStorage");
        }
        storage = new File(root, collectionName);
        if (!storage.exists()) {
            storage.mkdirs();
        }
    }

    public static File getAppDataDir(String appName) {
        String os = System.getProperty("os.name").toLowerCase();
        String home = System.getProperty("user.home");

        if (os.contains("win")) {
            // Windows: %APPDATA%\AppName
            return Paths.get(System.getenv("APPDATA"), appName).toFile();
        } else if (os.contains("mac")) {
            // macOS: ~/Library/Application Support/AppName
            return Paths.get(home, "Library", "Application Support", appName).toFile();
        } else {
            // Linux: ~/.config/AppName
            return Paths.get(home, ".config", appName).toFile();
        }
    }
}
