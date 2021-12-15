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
    }

    public String getFileName(Object object) {
        return "";
    }
    public void saveData(Object object, String key, String value) throws IllegalAccessException, IOException {
        checkCollection();
    }
    public <T> T getObject(String key, String value) throws IllegalAccessException, InstantiationException, IOException, TypeNotSetException {
        if (type == null) throw new TypeNotSetException();
        return (T) getObject(key, value, type);
    }
    public <T> T getObject(String key, String value, Class<T> clazz) throws IllegalAccessException, InstantiationException, IOException {
        checkCollection();
        //This method is for unique searches Ex. UUID
        return (T) null;
    }

    public void checkCollection() {
        if (root == null) root = new File("ObjectStorage");
    }

    public List<Object> getObjects(Class<?> clazz) {
        return null;
    }
}
