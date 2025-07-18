/*
 * Copyright (c) Christopher Willett 2021.
 * All Rights Reserved.
 */

package us.anvildevelopment.v1.util1.database;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mysql.cj.xdevapi.*;
import us.anvildevelopment.v1.util1.database.annotations.MemoryOnly;
import us.anvildevelopment.v1.util1.exceptions.TypeNotSetException;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import us.anvildevelopment.v1.util1.database.annotations.Key;
import us.anvildevelopment.v1.util1.database.annotations.MemoryOnly;
import us.anvildevelopment.v1.util1.exceptions.TypeNotSetException;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

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
        File folder = new File(storage, key);
        if (!folder.exists()) {
            folder.mkdirs();
        }
        File file = new File(folder, value + ".json");

        // Create a map for non-memory-only fields
        Object o = object;
        ObjectMapper map = new ObjectMapper();
        for (Field f : object.getClass().getDeclaredFields()) {
            if (!f.isAnnotationPresent(MemoryOnly.class)) {
                f.setAccessible(true);
                map.writeValue(file, object);
            }
        }
    }

    public <T> T getObject(String key, String value) throws IllegalAccessException, IOException, TypeNotSetException {
        if (type == null) throw new TypeNotSetException();
        return (T) getObject(key, value, type);
    }

    public <T> T getObject(String key, String value, Class<T> clazz) throws IllegalAccessException, IOException {
        checkCollection();
        File file = new File(new File(storage, key), value + ".json");
        if (!file.exists()) {
            return null; // Only read from disk, return null if file doesn't exist
        }
        return mapper.readValue(file, clazz);
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
                            Object obj = mapper.readValue(file, clazz);
                            objects.add(obj);
                        } catch (IOException e) {
                            System.err.println("Error reading file " + file.getName() + ": " + e.getMessage());
                        }
                    }
                }
            }
        }
        return objects; // Return empty list if no files found
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
}
