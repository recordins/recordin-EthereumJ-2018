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

public class AttrIntPositive extends Number implements Attr {

    /* Logger for console output */
    private static final Logger logger = LoggerFactory.getLogger(AttrIntPositive.class);

    /* Actual Max value */
    private static final int MAX_VALUE = Integer.MAX_VALUE;

    /* Actual Min value */
    private static final int MIN_VALUE = 0;

    /* Actual int value */
    protected int intValue = 0;

    public AttrIntPositive() {
        logger.trace("START AttrIntPositive()");
        logger.trace("END AttrIntPositive()");
    }

    /**
     * Initializes an instance object for positive {@code Integer} value
     * representation.
     *
     * @param value {@code Integer} representation of an {@link AttrIntPositive}
     */
    public AttrIntPositive(int value) {
        logger.trace("START AttrIntPositive(int)");
        this.intValue = value;
        logger.trace("END AttrIntPositive(int)");
    }

    /**
     * Initializes an instance object for positive {@code Integer} value
     * representation.
     *
     * @param value {@code String} representation of an {@code Integer}
     * @throws NumberFormatException if given {@code String} is not an
     *                               {@code Integer} representation or does not respect min and max values.
     */
    public AttrIntPositive(String value) throws AttrException {
        logger.trace("START AttrIntPositive(String)");

        if (value.equals("")) {
            value = "0";
        }

        long valueLong = 0L;

        valueLong = Long.parseLong(value);

        if (valueLong > MAX_VALUE || valueLong < MIN_VALUE) {
            throw new AttrException("Integer value must be between '" + MIN_VALUE + "' and '" + MAX_VALUE + "'");
        } else {
            this.intValue = (int) valueLong;
        }
        logger.trace("END AttrIntPositive(String)");
    }

    /**
     * Returns corresponding {@code AttrIntPositive} representation, for given
     * {@code String} representation
     *
     * @param value {@code String} representation
     * @return
     * @throws NumberFormatException If given {@code String} is not an
     *                               {@code Integer} representation or does not respect min and max values.
     */
    public static AttrIntPositive valueOf(String value) throws AttrException {
        logger.trace("START valueOf(String)");

        logger.trace("END valueOf(String)");
        return new AttrIntPositive(value);
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

        String result = String.format("%d", this.intValue);

        logger.trace("END toString()");
        return result;
    }

    /**
     * Returns {@code Integer} value of current instance.
     *
     * @return {@code Integer} value of current instance
     */
    @Override
    public int intValue() {
        logger.trace("START intValue()");

        logger.trace("END intValue()");
        return this.intValue;
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
        return (long) this.intValue;
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
        return this.intValue;
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
        return (double) this.intValue;
    }

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

        logger.trace("END compareTo(AttrIntPositive)");
        return Integer.compare(this.intValue, ((AttrIntPositive) attr).intValue());
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
        return this.intValue == ((AttrIntPositive) attr).intValue;
    }
}
