/*
 * Record'in
 *
 * Copyright (C) 2019 Blockchain Record'in Solutions
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.recordins.recordin.orm.core;

import com.recordins.recordin.orm.exception.ORMException;
import com.recordins.recordin.utils.DeepCopy;
import jdbm.PrimaryTreeMap;
import jdbm.RecordManager;
import jdbm.RecordManagerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class BlockchainIndex<K extends java.lang.Comparable, V> implements SortedMap<K, V> {

    /* Logger for console output */
    private static final Logger logger = LoggerFactory.getLogger(BlockchainIndex.class);
    public PrimaryTreeMap<K, V> treeMap = null;
    private String name = "";
    private String displayName = "";
    private RecordManager recordManager;
    private boolean isPublic = false;

    private BlockchainIndex() {
    }

    public BlockchainIndex(String name, String displayName, Path rootFolderPath, boolean isPublic) throws ORMException {
        this(name, rootFolderPath);
        this.isPublic = isPublic;
        this.displayName = displayName;
    }

    public BlockchainIndex(String name, Path rootFolderPath) throws ORMException {

        if (name.equals("")) {
            throw new ORMException("Index cannot have an empty name");
        }

        if (!Files.exists(rootFolderPath)) {
            try {
                Files.createDirectories(rootFolderPath);
            } catch (IOException ex) {
                logger.error("Error creating BlockchainObjectIndex root directory: " + ex.toString());
            }
        }

        this.name = name;
        this.displayName = name;

        try {
            String fileName = rootFolderPath.resolve(name).toString();
            recordManager = RecordManagerFactory.createRecordManager(fileName);

            String recordName = name + "TreeMap";
            treeMap = recordManager.<K, V>treeMap(recordName);
            ;

            logger.debug("'" + name + "' Index loaded");
        } catch (Exception ex) {
            logger.error("Error instanciating '" + name + "' index: " + ex.toString());
            ex.printStackTrace();
        }

    }

    public boolean isPublic() {
        return isPublic;
    }

    public void commit() {
        try {
            this.recordManager.commit();
        } catch (IOException e) {
            logger.error("Error commit of Index '" + this.name + "': " + e.toString());
            e.printStackTrace();
        }
    }

    public String getDisplayName() {
        return this.displayName;
    }

    @Override
    public Comparator comparator() {
        return treeMap.comparator();
    }

    @Override
    public SortedMap subMap(K fromKey, K toKey) {
        return treeMap.subMap(fromKey, toKey);
    }

    @Override
    public SortedMap headMap(K toKey) {
        return treeMap.headMap(toKey);
    }

    @Override
    public SortedMap tailMap(K fromKey) {
        return treeMap.tailMap(fromKey);
    }

    @Override
    public K firstKey() {
        return treeMap.firstKey();
    }

    @Override
    public K lastKey() {
        return treeMap.lastKey();
    }

    @Override
    public int size() {
        return treeMap.size();
    }

    @Override
    public boolean isEmpty() {
        return treeMap.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return treeMap.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return treeMap.containsValue(value);
    }

    @Override
    public V get(Object key) {

        V result = treeMap.get(key);

        if (result != null && List.class.isAssignableFrom(result.getClass())) {
            result = (V) DeepCopy.copy(result);
        }

        return result;
    }

    @Override
    public V put(K key, V value) {
        return treeMap.put(key, value);
    }

    @Override
    public V remove(Object key) {
        return treeMap.remove(key);
    }

    @Override
    public void putAll(Map m) {
        treeMap.putAll(m);
    }

    @Override
    public void clear() {
        treeMap.clear();
    }

    @Override
    public Set keySet() {
        return treeMap.keySet();
    }

    @Override
    public Collection values() {
        return treeMap.values();
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        return treeMap.entrySet();
    }
}
