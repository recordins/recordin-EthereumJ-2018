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
import org.json.simple.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AttrEnum extends AttrString {

    /* Logger for console output */
    private static final Logger logger = LoggerFactory.getLogger(AttrEnum.class);

    /**
     * Initializes an instance object for {@code AttrEnum}.
     */
    public AttrEnum() throws AttrException {
        super();
    }

    /**
     * Initializes an instance object for {@code AttrEnum} value
     * representation.
     *
     * @param jsonArray {@code String} representation of an {@link AttrEnum}
     */
    public AttrEnum(JSONArray jsonArray) {
        super(jsonArray);
    }

    /**
     * Initializes an instance object for {@code AttrEnum} value
     * representation.
     *
     * @param value {@code String} representation of an {@link AttrEnum}
     */
    public AttrEnum(String value) {
        super(value);
    }

}
