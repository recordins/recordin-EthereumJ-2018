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

import static com.recordins.recordin.utils.DateFormat.LongtoDate;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

import org.json.simple.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.recordins.recordin.utils.DateFormat.StringtoDateTime;
import static com.recordins.recordin.utils.DateFormat.DateTimetoString;

public class AttrDateTime extends AttrLongPositive {

    /* Logger for console output */
    private static final Logger logger = LoggerFactory.getLogger(AttrDateTime.class);

    /* Actual Max value */
    private static final long MAX_VALUE = Long.MAX_VALUE;

    /* Actual Min value */
    private static final long MIN_VALUE = 0;

    /* Actual long value */
    // private long longValue = 0L;

    /* Actual String value */
    //@JsonProperty("stringValue")
    protected String stringValue = "";

    /**
     * Initializes an instance object for current date time value
     * representation.
     */
    public AttrDateTime() throws AttrException {
        this(LocalDateTime.now(ZoneOffset.UTC).toInstant(ZoneOffset.UTC).toEpochMilli());
        logger.trace("START AttrDateTime()");
        logger.trace("END AttrDateTime()");
    }

    /**
     * Initializes an instance object for date time value representation.
     *
     * @param value {@code Long} representation of an {@link AttrDateTime}
     * @throws AttrException if given {@code Long} is not an
     *                       {@link AttrDateTime} representation or does not respect min and max
     *                       values.
     */
    public AttrDateTime(Long value) throws AttrException {
        logger.trace("START AttrDateTime(Long)");

        if (value < MIN_VALUE) {
            throw new AttrException("Long value must be between '" + MIN_VALUE + "' and '" + MAX_VALUE + "'");
        } else {
            this.longValue = value;
        }
        logger.trace("END AttrDateTime()");
    }

    /**
     * Initializes an instance object for date time value representation.
     *
     * @param jsonArray {@code JSONArray} representation of an
     *                  {@link AttrDateTime}
     * @throws AttrException if given {@code JSONArray} is not an
     *                       {@link AttrDateTime} representation or does not respect min and max
     *                       values.
     */

    public AttrDateTime(JSONArray jsonArray) throws AttrException {
        logger.trace("START AttrDateTime(JSONArray)");

        String value = "";
        if (jsonArray != null && jsonArray.size() > 1) {
            value = (String) jsonArray.get(1);
        }

        LocalDateTime date = StringtoDateTime(value);
        this.longValue = date.toInstant(ZoneOffset.UTC).toEpochMilli();

        logger.trace("END AttrDateTime()");
    }


    /**
     * Initializes an instance object for date time value representation.
     *
     * @param value {@code String} representation of an {@link AttrDateTime}
     * @throws AttrException if given {@code String} is not an
     *                       {@link AttrDateTime} representation or does not respect min and max
     *                       values.
     */
    public AttrDateTime(String value) throws AttrException {
        logger.trace("START AttrDateTime(Long)");

        LocalDateTime date = StringtoDateTime(value);
        this.longValue = date.toInstant(ZoneOffset.UTC).toEpochMilli();

        logger.trace("END AttrDateTime()");
    }

    /**
     * Returns corresponding {@code AttrDateTime} representation, for given
     * {@code String} representation
     *
     * @param value {@code String} representation
     * @return
     * @throws NumberFormatException If given {@code String} is not an
     *                               {@link AttrDateTime} representation or does not respect min and max
     *                               values.
     */
    public static AttrDateTime valueOf(String value) throws AttrException {
        logger.trace("START valueOf(String)");

        logger.trace("END valueOf()");
        return new AttrDateTime(value);
    }

    @Override
    @JsonIgnore
    public String getFormat() {

        return "\"dd/mm/yyyy hh:mm:ss\"";
    }

    /**
     * Returns {@code String} representation of current instance.
     *
     * @return {@code String} representation of current instance
     */
    @Override
    public String toString() {
        logger.trace("START toString()");

        String result = DateTimetoString(LongtoDate(this.longValue));

        logger.trace("END toString()");
        return "\"" + result + "\"";
    }

    public String toStringCleared() {
        logger.trace("START toStringCleared()");

        String result = toString();
        result = result.substring(1);
        result = result.substring(0, result.length() - 2);

        logger.trace("END toStringCleared()");
        return result;
    }
    
    /*
    @Override
    public int intValue() {
        logger.debug("START intValue()");

        logger.debug("END intValue()");
        return (int) this.longValue;
    }
    */

    /**
     * Returns {@code Long} value of current instance.
     *
     * @return {@code Long} value of current instance
     *
     */
    // @Override
    /*
    public long longValue() {
        logger.debug("START longValue()");

        logger.debug("END longValue()");
        return this.longValue;
    }
    */

    /*
    /**
     * Returns {@code Float} value of current instance.
     *
     * @return {@code Float} value of current instance
     *
     *
    @Override
    public float floatValue() {
        logger.debug("START floatValue()");

        logger.debug("END floatValue()");
        return this.longValue;
    }

    /**
     * Returns {@code Double} value of current instance.
     *
     * @return {@code Double} value of current instance
     *
     *
    @Override
    public double doubleValue() {
        logger.debug("START doubleValue()");

        logger.debug("END doubleValue()");
        return (double) this.longValue;
    }
    */

    /**
     * Compares current {@link AttrDateTime} value with {@link AttrDateTime}
     * value of given instance.
     *
     * @param attr the object to be compared.
     * @return a negative {@code Integer}, zero, or a positive {@code Integer}
     * as this object is less than, equal to, or greater than the given
     * instance.
     */
    @Override
    public int compareTo(Attr attr) {
        logger.trace("START compareTo(AttrDateTime)");

        if (!attr.getClass().isAssignableFrom(this.getClass())) {
            throw new AttrComparisonTypeException(attr.getClass(), this.getClass());
        }

        logger.trace("END compareTo()");
        return Long.compare(this.longValue, ((AttrDateTime) attr).longValue());
    }

    /**
     * Compares current {@link AttrDateTime} value with {@link AttrDateTime}
     * value of given instance.
     *
     * @param attr the object to be compared.
     * @return a {@code boolean} whether both objects are identical
     */
    @Override
    public boolean equals(Attr attr) {
        logger.trace("START equals(AttrDateTime)");

        if (!attr.getClass().isAssignableFrom(this.getClass())) {
            throw new AttrComparisonTypeException(attr.getClass(), this.getClass());
        }

        logger.trace("END equals()");
        return this.longValue == ((AttrDateTime) attr).longValue;
    }

    /*
    @JsonIgnore
    public String getStringValue() {
        logger.debug("START getStringValue()");
        logger.debug("END getStringValue()");
        return toString();
    }

    public void setStringValue(String stringValue) {
        //try {
        logger.debug("START setStringValue(String)");
        LocalDateTime date = StringtoDate(stringValue);
        this.longValue = date.toInstant(ZoneOffset.UTC).toEpochMilli();
        this.stringValue = stringValue;
        /*
        } catch (AttrException ex) {
            logger.trace("Error setting String value to date: " + stringValue + ": " + ex.toString());
        }
         *
        
        logger.debug("END setStringValue()");
    }
*/
}
