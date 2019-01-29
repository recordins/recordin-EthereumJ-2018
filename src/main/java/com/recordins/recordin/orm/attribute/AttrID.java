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
import com.recordins.recordin.Main;
import com.recordins.recordin.orm.attribute.exception.AttrException;
import com.recordins.recordin.orm.exception.ORMException;
import org.json.simple.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AttrID extends AttrString {

    /* Logger for console output */
    private static final Logger logger = LoggerFactory.getLogger(AttrID.class);

    public static String REGEX_PATTERN = "[0-9]+\\..+\\..+";

    public AttrID() {
        logger.trace("START AttrID()");
        this.stringValue = "";
        logger.trace("END AttrID()");
    }

    /**
     * Initializes an instance object for a blockchain's ID reference.
     *
     * @param jsonArray {@code JSONArray} representation of a {@link AttrID}
     * @throws NumberFormatException if given {@code JSONArray} is not an
     *                               {@link AttrID} representation or does not respect min and max values.
     */
    public AttrID(JSONArray jsonArray) throws AttrException {
        super(jsonArray);
        logger.trace("START AttrID(JSONArray)");

        if (!Main.initDataModelFlag && !this.stringValue.equals("") && !this.stringValue.matches(REGEX_PATTERN)) {

            throw new AttrException("Provided value does not match with AttrID pattern: '" + this.stringValue + "'");

        }

        logger.trace("END AttrID()");
    }

    /**
     * Initializes an instance object for a blockchain's ID reference.
     *
     * @param value {@code String} representation of a {@link AttrID}
     * @throws NumberFormatException if given {@code String} is not an
     *                               {@link AttrID} representation or does not respect min and max values.
     */
    public AttrID(String value) throws AttrException {
        super(value);
        logger.trace("START AttrID(String)");

        if (!Main.initDataModelFlag && !value.equals("") && !value.matches(REGEX_PATTERN)) {

            throw new AttrException("Provided value does not match with AttrID pattern: '" + value + "'");

        }

        logger.trace("END AttrID()");
    }

    @Override
    @JsonIgnore
    public String getFormat() {

        return "regex: \"" + REGEX_PATTERN + "\"";
    }

    @JsonIgnore
    public final long getBlockID() {
        long result = -1l;


        if (this.stringValue != null) {

            if (this.stringValue.contains(".")) {

                String[] values = this.stringValue.split("\\.");

                if (values.length > 0) {
                    result = Long.parseLong(values[0]);
                }
            }
        }
        return result;
    }

    @JsonIgnore
    public final String getTransactionID() {
        String result = "";

        if (this.stringValue != null) {
            if (this.stringValue.contains(".")) {

                String[] values = this.stringValue.split("\\.");

                if (values.length > 1) {
                    result = values[1];
                }
            }
        }
        return result;
    }

    @JsonIgnore
    public final String getUID() {
        String result = "";

        if (this.stringValue != null) {
            if (this.stringValue.contains(".")) {
                String[] values = this.stringValue.split("\\.");

                if (values.length > 2) {
                    result = values[2];
                }
            }
        }
        return result;
    }
}
