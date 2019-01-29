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

package com.recordins.recordin.orm.action;

import com.recordins.recordin.orm.attribute.exception.AttrComparisonTypeException;

import java.io.Serializable;
import java.util.Objects;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ActionDefinition implements Comparable<ActionDefinition>, Serializable {

    /* Logger for console output */
    private static final Logger logger = LoggerFactory.getLogger(ActionDefinition.class);

    public String actionName = "";
    public String actionDisplayName = "";
    public String actionType = ""; // Execute, SelectObject, SelectObjectList
    public String actionArgs = ""; // model name for action

    /* Actual string value */
    protected String stringValue = "";

    /**
     * Initializes an instance object for {@code ActionDefinition}
     */
    public ActionDefinition() {
        logger.trace("START ActionDefinition()");
        this.stringValue = "[" + actionName + "," + actionDisplayName + "," + actionType + "," + actionArgs + "]";
        logger.trace("END ActionDefinition()");
    }

    public ActionDefinition(String actionName, String actionDisplayName, String actionType, String actionArgs) {
        logger.trace("START ActionDefinition(String, String, String)");

        this.actionName = actionName;
        this.actionDisplayName = actionDisplayName;
        this.actionType = actionType;
        this.actionArgs = actionArgs;
        this.stringValue = "[\"" + actionName + "\",\"" + actionDisplayName + "\",\"" + actionType + "\",\"" + actionArgs + "\"]";

        logger.trace("END ActionDefinition()");
    }

    /**
     * Initializes an instance object for {@code ActionDefinition} value
     * representation.
     *
     * @param jsonArray {@code String} representation of an {@link ActionDefinition}
     */
    public ActionDefinition(JSONArray jsonArray) {
        logger.trace("START ActionDefinition(JSONArray)");

        if (jsonArray.size() > 0) {
            this.actionName = (String) jsonArray.get(0);
        }

        if (jsonArray.size() > 1) {
            this.actionDisplayName = (String) jsonArray.get(1);
        }

        if (jsonArray.size() > 2) {
            this.actionType = (String) jsonArray.get(1);
        }

        if (jsonArray.size() > 3) {
            this.actionArgs = (String) jsonArray.get(2);
        }

        logger.trace("END ActionDefinition()");
    }

    /**
     * Initializes an instance object for {@code ActionDefinition} value
     * representation.
     *
     * @param value {@code String} representation of an {@link ActionDefinition}
     */
    public ActionDefinition(String value) {
        logger.trace("START ActionDefinition(String)");

        JSONParser parser = new JSONParser();
        try {
            JSONArray jsonArray = (JSONArray) parser.parse(value);

            if (jsonArray.size() > 0) {
                this.actionName = (String) jsonArray.get(0);
            }

            if (jsonArray.size() > 1) {
                this.actionDisplayName = (String) jsonArray.get(0);
            }

            if (jsonArray.size() > 2) {
                this.actionType = (String) jsonArray.get(1);
            }

            if (jsonArray.size() > 3) {
                this.actionArgs = (String) jsonArray.get(2);
            }

        } catch (ParseException ex) {
            logger.error("Error parsing JSONArray for ActionDefinition: " + ex.toString());
        }

        logger.trace("END ActionDefinition()");
    }

    public JSONArray getJSONArray() {
        logger.trace("START getJSONObject()");
        JSONParser parser = new JSONParser();
        JSONArray jsonArray = new JSONArray();
        try {
            jsonArray.add(actionName);
            jsonArray.add(actionDisplayName);
            jsonArray.add(actionType);

            JSONObject jsonObj = (JSONObject) parser.parse(actionArgs);

            jsonArray.add(jsonObj);


        } catch (ParseException ex) {
            logger.error("Error parsing stringValue for getJSONArray: " + ex.toString());
        }
        /*
        JSONArray jsonArray = new JSONArray();
        String[] parts = stringValue.split(",");
        JSONObject jsonObj = new JSONObject();
        String part1 = parts[0];
        String part2 = parts[1];
        String part3 = parts[2];
        String part4 = parts[3];
        part1 = part1.substring(2, part1.length() - 1);
        part4 = part4.substring(1, part4.length() - 2);

            jsonObj = (JSONObject) parser.parse(part4);
            jsonArray.add(part1);
            jsonArray.add(part2);
            jsonArray.add(part3);
            jsonArray.add(jsonObj);
        } catch (ParseException ex) {
            logger.error("Error parsing JSONObject for getJSONObject: " + ex.toString());
        }
        */
        logger.trace("END getJSONObject()");
        return jsonArray;
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
     * Compares current {@link ActionDefinition} value with
     * {@link ActionDefinition} value of given instance.
     *
     * @param actionDefinition the object to be compared.
     * @return a {@code boolean} whether both objects are identical
     */
    public boolean equals(ActionDefinition actionDefinition) {
        logger.trace("START equals(ActionDefinition)");

        if (!actionDefinition.getClass().isAssignableFrom(this.getClass())) {
            throw new AttrComparisonTypeException(actionDefinition.getClass(), this.getClass());
        }

        logger.trace("END equals()");
        return this.stringValue.equals(((ActionDefinition) actionDefinition).toString());
    }

    @Override
    public boolean equals(Object actionDefinition) {
        logger.trace("START equals(Object)");

        if (!actionDefinition.getClass().isAssignableFrom(this.getClass())) {
            return super.equals(actionDefinition);
        }

        logger.trace("END equals()");
        return this.equals(((ActionDefinition) actionDefinition));
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 41 * hash + Objects.hashCode(this.stringValue);
        return hash;
    }

    @Override
    public int compareTo(ActionDefinition actionDefinition) {
        logger.trace("START compareTo(ActionDefinition)");

        if (!actionDefinition.getClass().isAssignableFrom(this.getClass())) {
            throw new AttrComparisonTypeException(actionDefinition.getClass(), this.getClass());
        }

        logger.trace("END compareTo()");
        return this.stringValue.compareTo(actionDefinition.toString());
    }
}
