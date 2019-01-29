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
import org.json.simple.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public class AttrString implements Attr, CharSequence {

    /* Logger for console output */
    private static final Logger logger = LoggerFactory.getLogger(AttrString.class);

    /* Actual string value */
    protected String stringValue = "";

    /**
     * Initializes an instance object for {@code String} with empty value
     * representation.
     */
    public AttrString() {
        this("");
        logger.trace("START AttrString()");
        logger.trace("START AttrString()");
    }

    /**
     * Initializes an instance object for {@code JSONArray} value
     * representation..
     *
     * @param jsonArray {@code JSONArray} representation of an {@link AttrString}
     */
    public AttrString(JSONArray jsonArray) {
        logger.trace("START AttrString(JSONArray)");

        if (jsonArray != null && jsonArray.size() > 1) {
            this.stringValue = (String) jsonArray.get(1);
        }

        logger.trace("END AttrString()");
    }

    /**
     * Initializes an instance object for {@code String} value representation..
     *
     * @param value {@code String} representation of an {@link AttrString}
     */
    public AttrString(String value) {
        logger.trace("START AttrString(String)");

        this.stringValue = value;

        logger.trace("END AttrString()");
    }

    /**
     * Returns corresponding {@link AttrString} representation, for given
     * {@code String} representation
     *
     * @param value {@code String} representation
     * @return
     */
    public static AttrString valueOf(String value) throws NumberFormatException {
        logger.trace("START valueOf(String)");

        logger.trace("END valueOf()");
        return new AttrString(value);
    }

    @Override
    @JsonIgnore
    public String getFormat() {

        return "string value";
    }

    /**
     * Returns {@code String} representation of current instance.
     *
     * @return {@code String} representation of current instance
     */
    @Override
    public String toString() {
        logger.trace("START toString()");

        logger.trace("END toString()");
        return stringValue;
    }

    /**
     * Compares current {@link AttrString} value with {@link AttrString} value
     * of given instance.
     *
     * @param attr the object to be compared.
     * @return a negative {@code Integer}, zero, or a positive {@code Integer}
     * as this object is less than, equal to, or greater than the given
     * instance.
     */
    @Override
    public int compareTo(Attr attr) {
        logger.trace("START compareTo(AttrString)");

        if (!attr.getClass().isAssignableFrom(this.getClass())) {
            throw new AttrComparisonTypeException(attr.getClass(), this.getClass());
        }

        logger.trace("END compareTo()");
        return toString().compareTo(attr.toString());
    }

    /**
     * Compares current {@link AttrString} value with {@link AttrString} value
     * of given instance.
     *
     * @param attr the object to be compared.
     * @return a {@code boolean} whether both objects are identical
     */
    @Override
    public boolean equals(Attr attr) {
        logger.trace("START equals(AttrString)");

        if (!attr.getClass().isAssignableFrom(this.getClass())) {
            throw new AttrComparisonTypeException(attr.getClass(), this.getClass());
        }

        logger.trace("END equals()");
        return toString().equals(((AttrString) attr).toString());
    }

    /**
     * Compares current {@link AttrString} value with {@link AttrString} value
     * of given instance.
     *
     * @param string the object to be compared.
     * @return a {@code boolean} whether both objects are identical
     */

    public boolean equals(String string) {
        logger.trace("START equals(String)");
        logger.trace("END equals()");
        return toString().equals(string);
    }

    @Override
    public int hashCode() {
        logger.trace("START hashCode()");
        logger.trace("END hashCode()");
        return toString().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        logger.trace("START equals(Object)");

        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }

        logger.trace("END equals()");
        return ((AttrString) this).equals((AttrString) obj);
    }

    @Override
    public int length() {
        return toString().length();
    }

    @Override
    public char charAt(int index) {
        return toString().charAt(index);
    }

    @Override
    public CharSequence subSequence(int start, int end) {
        return toString().subSequence(start, end);
    }
}
