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
import com.recordins.recordin.orm.BlockchainObject;
import com.recordins.recordin.orm.attribute.exception.AttrComparisonTypeException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import com.recordins.recordin.orm.attribute.exception.AttrException;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AttrIDList extends ArrayList<AttrID> implements Attr {

    /* Logger for console output */
    private static final Logger logger = LoggerFactory.getLogger(AttrIDList.class);

    /* Actual list of {@link AttrID} */
    //protected List<AttrID> attrList = null;

    /**
     * Initializes a list of {@link AttrID}.
     */
    public AttrIDList() {
        super();
        logger.trace("START AttrIDList()");
        //    this.attrList = Collections.synchronizedList(new ArrayList());
        logger.trace("END AttrIDList()");
    }

    /**
     * Initializes a list of {@link AttrID} with provided {@code Array} data.
     *
     * @param objectArray, the provided data to initialize the {@link AttrIDList}
     */
    public AttrIDList(BlockchainObject... objectArray) {
        super();
        logger.trace("START AttrIDList(objectArray)");
        //ArrayList<AttrID> list = new ArrayList();

        for (int i = 0; i < objectArray.length; i++) {
            //list.add(idArray[i]);
            add(objectArray[i].getId());
        }
        //this.attrList = Collections.synchronizedList(list);
        logger.trace("END AttrIDList()");
    }

    /**
     * Initializes a list of {@link AttrID} with provided {@code Array} data.
     *
     * @param idArray, the provided data to initialize the {@link AttrIDList}
     */
    public AttrIDList(AttrID... idArray) {
        super();
        logger.trace("START AttrIDList(idArray)");
        //ArrayList<AttrID> list = new ArrayList();

        for (int i = 0; i < idArray.length; i++) {
            //list.add(idArray[i]);
            add(idArray[i]);
        }
        //this.attrList = Collections.synchronizedList(list);
        logger.trace("END AttrIDList()");
    }

    /**
     * Initializes a list of {@link AttrID} with provided {@code List} data.
     *
     * @param list, the provided data to initialize the {@link AttrIDList}
     */
    public AttrIDList(List<AttrID> list) {
        super(list);
        logger.trace("START AttrIDList(List)");
        //this.attrList = Collections.synchronizedList(new ArrayList(list));
        logger.trace("END AttrIDList()");
    }

    /**
     * Initializes a list of {@link AttrID}.
     *
     * @param jsonArray {@code JSONArray} representation of a
     *                  {@link AttrID} {@code List}
     */
    public AttrIDList(JSONArray jsonArray) throws AttrException {
        super();
        logger.trace("START AttrIDList(JSONArray)");

        if (jsonArray != null && jsonArray.size() > 0) {

            JSONArray jsonList = (JSONArray) jsonArray.get(1);

            if (jsonList != null && jsonList.size() > 0) {

                for (int i = 0; i < jsonList.size(); i++) {
                    String idString = (String) ((JSONArray) jsonList.get(i)).get(1);
                    if (!idString.equals("")) {
                        AttrID attrID = new AttrID(idString);
                        this.add(attrID);
                    }
                }
            }
        }

        logger.trace("END AttrIDList()");
    }

    public AttrIDList(String value) throws AttrException {
        super();
        logger.trace("START AttrIDList(String) VALUE: " + value);

        //this.attrList = Collections.synchronizedList(new ArrayList());

        JSONParser parser = new JSONParser();
        JSONArray jsonArray;
        try {
            jsonArray = (JSONArray) parser.parse(value);

            if (jsonArray != null && jsonArray.size() > 0) {
                AttrID attrID;

                for (int i = 0; i < jsonArray.size(); i++) {
                    String idString = (String) ((JSONArray) jsonArray.get(i)).get(1);
                    if (!idString.equals("")) {
                        attrID = new AttrID(idString);
                        add(attrID);
                    }
                }
            }
        } catch (ParseException ex) {
            logger.error("Error parsing String for AttrIDList: " + ex.toString());
        }

        logger.trace("END AttrIDList()");
    }

    @Override
    @JsonIgnore
    public String getFormat() {

        return "json array of AttrID: [\"com.recordins.recordin.orm.attribute.AttrIDList\",[[\"com.recordins.recordin.orm.attribute.AttrID\", \"AttrID value string\"]]";
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
            //string.append("\"" + attr + "\"");
            string.append(attr);

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
     * Compares current {@link AttrIDList} value with {@link AttrIDList} value
     * of given instance.
     *
     * @param attr the object to be compared.
     * @return a negative {@code Integer}, zero, or a positive {@code Integer}
     * as this object is less than, equal to, or greater than the given
     * instance.
     */
    @Override
    public int compareTo(Attr attr) {
        logger.trace("START compareTo(AttrIDList)");

        if (!attr.getClass().isAssignableFrom(this.getClass())) {
            throw new AttrComparisonTypeException(attr.getClass(), this.getClass());
        }

        logger.trace("END compareTo()");
        return this.toString().compareTo(attr.toString());
    }

    /**
     * Compares current {@link AttrList} value with {@link AttrList} value of
     * given instance.
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


    /**
     * Adds a list of {@link AttrID} with provided {@code Array} data.
     *
     * @param idArray, the provided data to add to the {@link AttrIDList}
     */
    public void add(AttrID... idArray) {
        logger.trace("START add(array)");

        for (int i = 0; i < idArray.length; i++) {
            //this.attrList.add(idArray[i]);
            this.add(idArray[i]);
        }

        logger.trace("END add()");
    }

    public AttrID getLast() {
        //if(this.attrList.size()>0) { return this.attrList.get(0);} else{ return null; }
        if (this.size() > 0) {
            return this.get(0);
        } else {
            return null;
        }
    }

    public boolean containsUID(AttrID objectID) {

        for (AttrID id : this) {
            if (id.getUID().equals(objectID.getUID())) {
                return true;
            }
        }
        return false;
    }

    public void removeUID(AttrID objectID) {

        for (AttrID id : this) {
            if (id.getUID().equals(objectID.getUID())) {
                remove(id);
            }
        }
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

    //  @Override
    public boolean contains(AttrID attr) {
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
    public boolean add(AttrID e) {
        return this.attrList.add(e);
    }

    @Override
    public boolean remove(Object o) {
        return this.attrList.remove(o);
    }

    public boolean remove(AttrID attr) {
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
    public AttrID get(int index) {
        return this.attrList.get(index);
    }

    @Override
    public AttrID set(int index, AttrID element) {
        return this.attrList.set(index, element);
    }

    @Override
    public void add(int index, AttrID element) {
        this.attrList.add(index, element);
    }

    @Override
    public AttrID remove(int index) {
        return this.attrList.remove(index);
    }

    @Override
    public int indexOf(Object o) {
        return this.attrList.indexOf(o);
    }

    public int indexOf(AttrID attr) {
        return this.attrList.indexOf(attr);
    }

    @Override
    public int lastIndexOf(Object o) {
        return this.attrList.lastIndexOf(o);
    }

    public int lastIndexOf(AttrID attr) {
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
