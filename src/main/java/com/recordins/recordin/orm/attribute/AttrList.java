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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AttrList<T extends Attr> extends ArrayList<Attr> implements Attr {

    /* Logger for console output */
    private static final Logger logger = LoggerFactory.getLogger(AttrList.class);

    /* Actual list of {@link Attr} */
    //protected List<Attr> attrList;

    /**
     * Initializes a list of {@link Attr}.
     */
    public AttrList() {
        super();
        logger.trace("START AttrList()");
        //this.attrList = Collections.synchronizedList(new ArrayList());
        logger.trace("END AttrList()");
    }

    /**
     * Initializes a list of {@link Attr} with provided {@code List} data.
     *
     * @param list, the provided data to initialize the {@link AttrList}
     */
    public AttrList(List<Attr> list) {
        super(list);
        logger.trace("START List()");
        //this.attrList = Collections.synchronizedList(new ArrayList(list));
        logger.trace("END List()");
    }

    public AttrList(String value) {
        super();
        logger.trace("START AttrList(String)");

        //this.attrList = Collections.synchronizedList(new ArrayList());

        JSONParser parser = new JSONParser();
        JSONArray jsonArray;
        try {
            jsonArray = (JSONArray) parser.parse(value);


            for (int i = 0; i < jsonArray.size(); i++) {
                Object object = jsonArray.get(i);

                if (JSONArray.class.isAssignableFrom(object.getClass())) {

                    String valueString = "";
                    try {
                        JSONArray attributeArray = (JSONArray) object;

                        //logger.debug("AttrList Class: " + attributeArray.get(0).toString());

                        Class[] stringArgsClass = new Class[]{String.class};
                        Class objectClass;
                        objectClass = Class.forName(attributeArray.get(0).toString());

                        Constructor stringArgsConstructor = objectClass.getConstructor(stringArgsClass);

                        valueString = String.valueOf(attributeArray.get(1));

                        Object[] stringArgs;
                        if (attributeArray.get(1) != null) {
                            stringArgs = new Object[]{valueString};
                        } else {
                            stringArgs = new Object[]{"{}"};
                        }

                        Attr attr = (Attr) stringArgsConstructor.newInstance(stringArgs);
                        //this.attrList.add(attr);
                        this.add(attr);
                    } catch (ClassNotFoundException | NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                        logger.error("Error reading AttrList with value: " + valueString + ": " + ex.toString());
                    }
                }
            }
        } catch (ParseException ex) {
            logger.error("Error parsing String for AttrList: " + ex.toString());
        }

        logger.trace("END AttrList()");
    }

    @Override
    @JsonIgnore
    public String getFormat() {

        return "json array of Attr: [\"com.recordins.recordin.orm.attribute.Attrist\",[[\"Attr Class canonical name\", \"Attr value string\"]]";
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
        string.append("[");
        int counter = 0;
        //for (Attr attr : this.attrList) {
        for (Attr attr : this) {
            counter++;
            string.append(attr.toString());

            //if (counter < this.attrList.size()) {
            if (counter < this.size()) {
                string.append(",");
            }
        }
        string.append("]");

        logger.trace("END toString()");
        return string.toString();
    }

    /**
     * Compares current {@link AttrList} value with {@link AttrList} value of
     * given instance.
     *
     * @param attr the object to be compared.
     * @return a negative {@code Integer}, zero, or a positive {@code Integer}
     * as this object is less than, equal to, or greater than the given
     * instance.
     */
    @Override
    public int compareTo(Attr attr) {
        logger.trace("START compareTo(AttrList)");

        if (!attr.getClass().isAssignableFrom(this.getClass())) {
            throw new AttrComparisonTypeException(attr.getClass(), this.getClass());
        }

        logger.trace("END compareTo()");
        return this.toString().compareTo(attr.toString());
    }

    /**
     * Compares current {@link AttrIntPositive} value with
     * {@link AttrIntPositive} value of given instance.
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
        return this.toString().equals(((AttrList) attr).toString());
    }

    /*
    @Override
    public int size() {
        return this.attrList.size();
    }

    @Override
    public boolean isEmpty() {
        return this.attrList.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return this.attrList.contains(o);
    }

    public boolean contains(Attr attr) {
        return this.attrList.contains(attr);
    }

    @Override
    public Iterator iterator() {
        return this.attrList.iterator();
    }

    @Override
    public Object[] toArray() {
        return this.attrList.toArray();
    }

    @Override
    public Object[] toArray(Object[] a) {
        return this.attrList.toArray(a);
    }

    @Override
    public boolean add(Attr e) {
        return this.attrList.add(e);
    }

    @Override
    public boolean remove(Object o) {
        return this.attrList.remove(o);
    }

    public boolean remove(Attr attr) {
        return this.attrList.remove(attr);
    }

    @Override
    public boolean containsAll(Collection c) {
        return this.attrList.containsAll(c);
    }

    @Override
    public boolean addAll(Collection c) {
        return this.attrList.addAll(c);
    }

    @Override
    public boolean addAll(int index, Collection c) {
        return this.attrList.addAll(index, c);
    }

    @Override
    public boolean removeAll(Collection c) {
        return this.attrList.removeAll(c);
    }

    @Override
    public boolean retainAll(Collection c) {
        return this.attrList.retainAll(c);
    }

    @Override
    public void clear() {
        this.attrList.clear();
    }

    @Override
    public Attr get(int index) {
        return this.attrList.get(index);
    }

    @Override
    public Attr set(int index, Attr element) {
        return this.attrList.set(index, element);
    }

    @Override
    public void add(int index, Attr element) {
        this.attrList.add(index, element);
    }

    @Override
    public Attr remove(int index) {
        return this.attrList.remove(index);
    }

    @Override
    public int indexOf(Object o) {
        return this.attrList.indexOf(o);
    }

    public int indexOf(Attr attr) {
        return this.attrList.indexOf(attr);
    }

    @Override
    public int lastIndexOf(Object o) {
        return this.attrList.lastIndexOf(o);
    }

    public int lastIndexOf(Attr attr) {
        return this.attrList.lastIndexOf(attr);
    }

    @Override
    public ListIterator listIterator() {
        return this.attrList.listIterator();
    }

    @Override
    public ListIterator listIterator(int index) {
        return this.attrList.listIterator(index);
    }

    @Override
    public List subList(int fromIndex, int toIndex) {
        return this.attrList.subList(fromIndex, toIndex);
    }
    */
}
