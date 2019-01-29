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
import com.recordins.recordin.orm.attribute.exception.AttrException;
import org.json.simple.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AttrBoolean extends AttrString {

    /* Logger for console output */
    private static final Logger logger = LoggerFactory.getLogger(AttrBoolean.class);

    /* Actual boolean value */
    protected boolean boolValue = false;

    /* Actual String value */
    //protected String stringValue = "false";

    public AttrBoolean() {
        logger.trace("START AttrBoolean()");

        logger.trace("END AttrBoolean()");
    }

    /**
     * Initializes an instance object for {@code Boolean} value representation.
     *
     * @param value {@code String} representation of an {@code Boolean}
     */
    public AttrBoolean(String value) {
        logger.trace("START AttrBoolean(String)");

        this.boolValue = Boolean.parseBoolean(value);
        this.stringValue = value;

        logger.trace("END AttrBoolean()");
    }

    /**
     * Initializes an instance object for {@code Boolean} value representation.
     *
     * @param value {@code Boolean} representation of an {@link AttrBoolean}
     */
    public AttrBoolean(boolean value) {
        logger.trace("START AttrBoolean(boolean)");
        this.boolValue = value;
        this.stringValue = "" + value;

        logger.trace("END AttrBoolean()");
    }

    public AttrBoolean(JSONArray jsonArray) throws AttrException {
        logger.trace("START AttrDateTime(JSONArray)");

        String value = "false";
        if (jsonArray != null && jsonArray.size() > 1) {
            value = (String) jsonArray.get(1);
            //value = (String) ((JSONObject) jsonArray.get(1)).get("stringValue");
        }

        this.boolValue = Boolean.parseBoolean(value);
        this.stringValue = "" + value;

        logger.trace("END AttrDateTime()");
    }

    /**
     * Returns corresponding {@code AttrBoolean} representation, for given
     * {@code String} representation
     *
     * @param value {@code String} representation
     * @return
     */
    public static AttrBoolean valueOf(String value) {
        logger.trace("START valueOf(String)");

        logger.trace("END valueOf()");
        return new AttrBoolean(value);
    }

    @Override
    @JsonIgnore
    public String getFormat() {

        return "\"true\" or \"false\" ";
    }

    /**
     * Returns {@code String} representation of current instance.
     *
     * @return {@code String} representation of current instance
     */
    @Override
    public String toString() {
        logger.trace("START toString()");

        String result = "" + this.boolValue;

        logger.trace("END toString()");
        return result;
        //return "\"" + result + "\"";
    }
    
    /*
    public String toStringCleared() {
        logger.trace("START toStringCleared()");

        String result = toString();
        result = result.substring(1);
        result = result.substring(0, result.length()-2);
        
        logger.trace("END toStringCleared()");
        return result;
    }
    */

    /**
     * Compares current {@link AttrIntPositive} value with
     * {@link AttrIntPositive} value of given instance.
     *
     * @param attr the object to be compared.
     * @return a negative {@code Integer}, zero, or a positive {@code Integer}
     * as this object is less than, equal to, or greater than the given
     * instance.
     */
    @Override
    public int compareTo(Attr attr) {
        logger.trace("START compareTo(AttrIntPositive)");

        if (!attr.getClass().isAssignableFrom(this.getClass())) {
            throw new AttrComparisonTypeException(attr.getClass(), this.getClass());
        }

        logger.trace("END compareTo()");
        return this.boolValue == ((AttrBoolean) attr).boolValue ? 0 : -1;
    }

    /**
     * Compares current {@link AttrBoolean} value with {@link AttrBoolean} value
     * of given instance.
     *
     * @param attr the object to be compared.
     * @return a {@code boolean} whether both objects are identical
     */
    @Override
    public boolean equals(Attr attr) {
        logger.trace("START equals(AttrBoolean)");

        if (!attr.getClass().isAssignableFrom(this.getClass())) {
            throw new AttrComparisonTypeException(attr.getClass(), this.getClass());
        }

        logger.trace("END equals()");
        return this.boolValue == ((AttrBoolean) attr).boolValue;
    }

    /*
    public String getStringValue() {
        logger.trace("START getStringValue()");
        logger.trace("END getStringValue()");
        return toString();
    }

    public void setStringValue(String stringValue) {
        logger.trace("START setStringValue()");
        this.stringValue = stringValue;
        this.boolValue = Boolean.parseBoolean(stringValue);
        logger.trace("END setStringValue()");
    }
    */

    @JsonIgnore
    public boolean isBoolValue() {
        logger.trace("START isBoolValue()");
        logger.trace("END isBoolValue()");
        return boolValue;
    }

    /*
    public void setBoolValue(boolean boolValue) {
        this.boolValue = boolValue;
    }
     */
}
