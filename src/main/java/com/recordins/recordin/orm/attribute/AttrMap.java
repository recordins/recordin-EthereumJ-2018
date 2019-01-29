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

package com.recordins.recordin.orm.attribute;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.recordins.recordin.orm.attribute.exception.AttrComparisonTypeException;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AttrMap implements Map<String, Attr>, Attr {

    /* Logger for console output */
    private static final Logger logger = LoggerFactory.getLogger(AttrMap.class);

    /* Actual map of {@link Attr} */
    protected Map<String, Attr> attrMap = null;

    /**
     * Initializes a map of {@link Attr}.
     */
    public AttrMap() {
        logger.trace("START AttrMap()");
        this.attrMap = new ConcurrentHashMap();
        logger.trace("START AttrMap()");
    }

    /**
     * Initializes a map of {@link Attr} with provided {@code Map} data.
     *
     * @param map, the provided data to initialize the {@link AttrMap}
     */
    public AttrMap(Map<String, Attr> map) {
        logger.trace("START AttrMap(Map)");
        this.attrMap = new ConcurrentHashMap(map);
        logger.trace("START AttrMap()");
    }


    public AttrMap(String value) {
        logger.trace("START AttrMap(String)");
        this.attrMap = new ConcurrentHashMap();
        logger.trace("END AttrMap()");
    }

    @Override
    @JsonIgnore
    public String getFormat() {

        return "AttrMap";
    }

    /**
     * Returns {@code String} representation of current instance.
     *
     * @return {@code String} representation of current instance
     */
    @Override
    public String toString() {
        logger.trace("START toString()");

        StringBuilder string = new StringBuilder();
        string.append("{");
        int counter = 0;
        for (Entry<String, Attr> entry : this.attrMap.entrySet()) {
            counter++;
            string.append("\"" + entry.getKey() + "\": \"" + entry.getValue() + "\"");

            if (counter < this.attrMap.size()) {
                string.append(",");
            }
        }

        string.append("}");

        logger.trace("END toString()");
        return string.toString();
    }

    /**
     * Compares current {@link AttrMap} value with {@link AttrMap} value of
     * given instance.
     *
     * @param attr the object to be compared.
     * @return a negative {@code Integer}, zero, or a positive {@code Integer}
     * as this object is less than, equal to, or greater than the given
     * instance.
     */
    @Override
    public int compareTo(Attr attr) {
        logger.trace("START compareTo(AttrMap)");

        if (!attr.getClass().isAssignableFrom(this.getClass())) {
            throw new AttrComparisonTypeException(attr.getClass(), this.getClass());
        }

        logger.trace("END compareTo()");
        return this.toString().compareTo(attr.toString());
    }

    /**
     * Compares current {@link AttrMap} value with
     * {@link AttrMap} value of given instance.
     *
     * @param attr the object to be compared.
     * @return a {@code boolean} whether both objects are identical
     */
    @Override
    public boolean equals(Attr attr) {
        logger.trace("START equals(Attr)");

        if (!attr.getClass().isAssignableFrom(this.getClass())) {
            throw new AttrComparisonTypeException(attr.getClass(), this.getClass());
        }

        logger.trace("END equals()");
        return this.toString().equals(((AttrMap) attr).toString());
    }

    @Override
    public int size() {
        return this.attrMap.size();
    }

    @Override
    public boolean isEmpty() {
        return this.attrMap.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return this.attrMap.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return this.attrMap.containsValue(value);
    }

    public boolean containsValue(Attr attr) {
        return this.attrMap.containsValue(attr);
    }

    @Override
    public Attr get(Object key) {
        return this.attrMap.get(key);
    }

    @Override
    public Attr put(String key, Attr value) {
        return this.attrMap.put(key, value);
    }

    @Override
    public Attr remove(Object key) {
        return this.attrMap.remove(key);
    }

    @Override
    public void putAll(Map<? extends String, ? extends Attr> m) {
        this.attrMap.putAll(m);
    }

    @Override
    public void clear() {
        this.attrMap.clear();
    }

    @Override
    public Set<String> keySet() {
        return this.attrMap.keySet();
    }

    @Override
    public Collection<Attr> values() {
        return this.attrMap.values();
    }

    @Override
    public Set<Entry<String, Attr>> entrySet() {
        return this.attrMap.entrySet();
    }
}
