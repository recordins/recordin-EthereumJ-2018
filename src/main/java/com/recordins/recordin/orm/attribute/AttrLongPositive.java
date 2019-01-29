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
import com.recordins.recordin.orm.attribute.exception.AttrException;
import com.recordins.recordin.orm.attribute.exception.AttrComparisonTypeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AttrLongPositive extends Number implements Attr {

    /* Logger for console output */
    private static final Logger logger = LoggerFactory.getLogger(AttrLongPositive.class);

    /* Actual Max value */
    private static final long MAX_VALUE = Long.MAX_VALUE;

    /* Actual Min value */
    private static final long MIN_VALUE = 0;

    /* Actual long value */
    protected long longValue = 0L;

    public AttrLongPositive() throws AttrException {
        logger.trace("START AttrLongPositive()");
        logger.trace("END AttrLongPositive()");
    }

    /**
     * Initializes an instance object for positive {@code Long} value
     * representation.
     *
     * @param value {@code Long} representation of an {@link AttrLongPositive}
     */
    public AttrLongPositive(Long value) throws AttrException {
        logger.trace("START AttrLongPositive(Long)");

        if (value < MIN_VALUE) {
            throw new AttrException("Long value must be between '" + MIN_VALUE + "' and '" + MAX_VALUE + "'");
        } else {
            this.longValue = value;
        }
        logger.trace("END AttrLongPositive()");
    }

    /**
     * Initializes an instance object for positive {@code Long} value
     * representation.
     *
     * @param value {@code String} representation of an {@link AttrLongPositive}
     * @throws NumberFormatException if given {@code String} is not an
     *                               {@code Long} representation or does not respect min and max values.
     */
    public AttrLongPositive(String value) throws AttrException {
        logger.trace("START AttrLongPositive(String)");

        if (value.equals("")) {
            value = "0";
        }

        long valueLong = Long.parseLong(value);

        if (valueLong < MIN_VALUE) {
            throw new AttrException("Long value must be between '" + MIN_VALUE + "' and '" + MAX_VALUE + "'");
        } else {
            this.longValue = valueLong;
        }

        logger.trace("END AttrLongPositive()");
    }

    /**
     * Returns corresponding {@link AttrLongPositive} representation, for given
     * {@code String} representation
     *
     * @param value {@code String} representation
     * @return
     * @throws NumberFormatException If given {@code String} is not an
     *                               {@code Long} representation or does not respect min and max values.
     */
    public static AttrLongPositive valueOf(String value) throws AttrException {
        logger.trace("START valueOf(String)");

        logger.trace("END valueOf()");
        return new AttrLongPositive(value);
    }

    @Override
    @JsonIgnore
    public String getFormat() {

        return "positive integer";
    }

    /**
     * Returns {@code String} representation of current instance.
     *
     * @return {@code String} representation of current instance
     */
    @Override
    public String toString() {
        logger.trace("START toString()");

        String result = String.format("%d", this.longValue);

        logger.trace("END toString()");
        return result;
    }

    /**
     * Returns {@code Long} value of current instance.
     *
     * @return {@code Long} value of current instance
     */
    @Override
    public int intValue() {
        logger.trace("START intValue()");

        logger.trace("END intValue()");
        return (int) this.longValue;
    }

    /**
     * Returns {@code Long} value of current instance.
     *
     * @return {@code Long} value of current instance
     */
    @Override
    public long longValue() {
        logger.trace("START longValue()");

        logger.trace("END longValue()");
        return this.longValue;
    }

    /**
     * Returns {@code Float} value of current instance.
     *
     * @return {@code Float} value of current instance
     */
    @Override
    public float floatValue() {
        logger.trace("START floatValue()");

        logger.trace("END floatValue()");
        return this.longValue;
    }

    /**
     * Returns {@code Double} value of current instance.
     *
     * @return {@code Double} value of current instance
     */
    @Override
    public double doubleValue() {
        logger.trace("START doubleValue()");

        logger.trace("END doubleValue()");
        return (double) this.longValue;
    }

    /**
     * Compares current {@link AttrLongPositive} value with
     * {@link AttrLongPositive} value of given instance.
     *
     * @param attr the object to be compared.
     * @return a negative {@code Integer}, zero, or a positive {@code Integer}
     * as this object is less than, equal to, or greater than the given
     * instance.
     */
    @Override
    public int compareTo(Attr attr) {
        logger.trace("START compareTo(AttrLongPositive)");

        if (!attr.getClass().isAssignableFrom(this.getClass())) {
            throw new AttrComparisonTypeException(attr.getClass(), this.getClass());
        }

        logger.trace("END compareTo()");
        return Long.compare(this.longValue, ((AttrLongPositive) attr).longValue());
    }

    /**
     * Compares current {@link AttrLongPositive} value with
     * {@link AttrLongPositive} value of given instance.
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
        return this.longValue == ((AttrLongPositive) attr).longValue;
    }
}
