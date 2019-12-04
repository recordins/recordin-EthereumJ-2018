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

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Map.Entry;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.cheetah.webserver.CheetahClassLoader;
import org.cheetah.webserver.CheetahWebserver;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AttrAttribute extends AttrString implements Attr {

    /* Logger for console output */
    private static final Logger logger = LoggerFactory.getLogger(AttrAttribute.class);

    public String Name;
    public String AttrType;
    public String AttrTypeModel;
    public String OnChange;
    public String DefaultValue;
    public String ContextData;
    public boolean Required;
    public boolean Unique;
    public boolean Visible;
    public boolean DisplayName;
    public boolean Indexed;
    public AttrIDList ACL;

    public AttrAttribute(String Name, String AttrType, String AttrTypeModel, String OnChange, String DefaultValue, String ContextData, boolean Required, boolean Unique, boolean Visible, boolean DisplayName, boolean Indexed, AttrIDList ACL) {
        this.Name = Name;
        this.AttrType = AttrType;
        this.AttrTypeModel = AttrTypeModel;
        this.OnChange = OnChange;
        this.DefaultValue = DefaultValue;
        this.ContextData = ContextData;
        this.Required = Required;
        this.Unique = Unique;
        this.Visible = Visible;
        this.DisplayName = DisplayName;
        this.Indexed = Indexed;
        this.ACL = ACL;

        stringValue = toString();
    }

    public AttrAttribute(String value) {
        logger.trace("START AttrAttribute(String)");

        JSONParser parser = new JSONParser();
        JSONObject jsonObject;
        try {

            //    logger.debug("1");
            jsonObject = (JSONObject) parser.parse(value);
            //    logger.debug("2");

            for (Object object : jsonObject.entrySet()) {

                //        logger.debug("3");
                try {
                    Entry entry = (Entry) object;
                    //        logger.debug("ATTRIBUTE 2: " + entry.getKey() + ": " + entry.getValue());

                    if (this.getClass().getField(entry.getKey().toString()) != null) {

                        //            logger.debug("ATTRIBUTE FOUND: " + entry.getKey() + ": " + entry.getValue());
                        if (entry.getValue() != null) {
                            if (JSONArray.class.isAssignableFrom(entry.getValue().getClass())) {
                                try {
                                    JSONArray attributeArray = (JSONArray) entry.getValue();

                                    if (attributeArray.size() > 0) {

                                        Class[] stringArgsClass = new Class[]{String.class};
                                        Class objectClass;
                                        //objectClass = Class.forName(attributeArray.get(0).toString());
                                        CheetahClassLoader cl;

                                        if (CheetahWebserver.getInstance() != null) {
                                            cl = CheetahWebserver.getInstance().getClassLoader();
                                        } else {
                                            cl = new CheetahClassLoader(Thread.currentThread().getContextClassLoader());
                                        }

                                        objectClass = cl.loadClass(attributeArray.get(0).toString());

                                        Constructor stringArgsConstructor = objectClass.getConstructor(stringArgsClass);

                                        Object[] stringArgs;
                                        if (attributeArray.get(1) != null) {
                                            stringArgs = new Object[]{String.valueOf(attributeArray.get(1))};
                                        } else {
                                            stringArgs = new Object[]{"{}"};
                                        }

                                        Attr attr = (Attr) stringArgsConstructor.newInstance(stringArgs);

                                        this.getClass().getField(entry.getKey().toString()).set(this, attr);
                                    } else {
                                        Field field = this.getClass().getField(entry.getKey().toString());
                                        Class type = field.getType();
                                        field.set(this, type.newInstance());
                                    }
                                } catch (ClassNotFoundException | NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                                    logger.error("Error reading AttrList: " + ex.toString());
                                }
                            } else {
                                this.getClass().getField(entry.getKey().toString()).set(this, entry.getValue());
                            }
                        } else {
                            this.getClass().getField(entry.getKey().toString()).set(this, entry.getValue());
                        }
                    }
                } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException ex) {
                    logger.error("Error reading AttrAttribute: " + ex.toString());
                    ex.printStackTrace();
                }

                stringValue = toString();
            }
        } catch (ParseException ex) {
            logger.error("Error parsing String for AttrList: " + ex.toString());
        }
        logger.trace("END AttrAttribute()");
    }

    @Override
    @JsonIgnore
    public String getFormat() {

        return "json object: {\"Name\": \"value string \", \"AttrType\": \"value string \", \"AttrTypeModel\": \"value string\", \"OnChange\" : \"json object string\", \"DefaultValue\": \"value string\", \"ContextData\": \"json object string\", \"Required\": [boolean true or false]  , \"Unique\": [boolean true or false]  , \"Visible\": [boolean true or false]  , \"DisplayName\": [boolean true or false]  ,  \"Indexed\": [true or false]  , \"ACL\": \"AttrIDList string\"}";
    }

    /**
     * Returns {@code String} representation of current instance.
     *
     * @return {@code String} representation of current instance
     */
    @Override
    public String toString() {

        String result = "[";

        for (Field field : this.getClass().getFields()) {

            try {
                result += "\"" + field.getName() + "\":";

                if (String.class.isAssignableFrom(field.getType())) {
                    result += "\"" + (String) field.get(this) + "\",";

                } else {
                    result += "" + String.valueOf(field.get(this)) + ",";
                }
            } catch (IllegalArgumentException | IllegalAccessException ex) {

            }
        }

        return result + "]";
    }
}
