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

import com.recordins.recordin.orm.attribute.exception.AttrException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.json.simple.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AttrPlatformVersion extends AttrString {

    /* Logger for console output */
    private static final Logger logger = LoggerFactory.getLogger(AttrPlatformVersion.class);

    /* Actual platform version value */
    private static String PLATFORM_VERSION = null;

    /**
     * Initializes an instance object for {@code AttrPlatformVersion} with value
     * representation from resource file {@code /com/interactive/recordin/orm/platform_version.properties}.
     */
    public AttrPlatformVersion() throws AttrException {
        logger.trace("START AttrPlatformVersion()");

        Properties properties = new Properties();
        InputStream resourceStream = AttrPlatformVersion.class.getResourceAsStream("/com/recordins/recordin/orm/platform_version.properties");

        if (PLATFORM_VERSION == null) {
            try {
                properties.load(resourceStream);
            } catch (IOException ex) {
                throw new AttrException("Error loading Platform Version properties file: " + ex.toString());
            }
            PLATFORM_VERSION = properties.getProperty("PlatformVersion");
        }

        this.stringValue = PLATFORM_VERSION;

        logger.trace("END AttrPlatformVersion()");
    }

    /**
     * Initializes an instance object for {@code AttrPlatformVersion} value
     * representation.
     *
     * @param jsonArray {@code String} representation of an {@link AttrPlatformVersion}
     */
    public AttrPlatformVersion(JSONArray jsonArray) {
        super(jsonArray);
        logger.trace("START AttrPlatformVersion(JSONArray)");
        logger.trace("END AttrPlatformVersion()");
    }

    /**
     * Initializes an instance object for {@code AttrPlatformVersion} value
     * representation.
     *
     * @param value {@code String} representation of an {@link AttrPlatformVersion}
     */
    public AttrPlatformVersion(String value) {
        super(value);
        logger.trace("START AttrPlatformVersion(String)");
        logger.trace("END AttrPlatformVersion()");
    }
    

    /*
    @Override
    public String toString() {
        logger.trace("START toString()");

        logger.trace("END toString()");
        return stringValue;
    }
    */

    /**
     * Compares current {@link AttrPlatformVersion} value with
     * {@link AttrPlatformVersion} value of given instance.
     *
     * @param attr the object to be compared.
     * @return a negative {@code Integer}, zero, or a positive {@code Integer}
     * as this object is less than, equal to, or greater than the given
     * instance.
     *
     */
    /*
    @Override
    public int compareTo(Attr attr) {
        logger.trace("START compareTo(AttrPlatformVersion)");

        if (!attr.getClass().isAssignableFrom(this.getClass())) {
            throw new AttrComparisonTypeException(attr.getClass(), this.getClass());
        }

        logger.trace("END compareTo(AttrPlatformVersion)");
        return this.stringValue.compareTo(attr.toString());
    }
*/

}
