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

package com.recordins.recordin.orm.core.objectfactory;

import com.recordins.recordin.orm.BlockchainObject;
import com.recordins.recordin.orm.attribute.Attr;
import com.recordins.recordin.orm.exception.ORMException;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

import org.cheetah.webserver.CheetahClassLoader;
import org.cheetah.webserver.CheetahWebserver;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FactoryPlatform_1_0 extends AbstractFactoryPlatform {

    private static Logger logger = LoggerFactory.getLogger(FactoryPlatform_1_0.class);

    public FactoryPlatform_1_0() {
    }

    @Override
    public BlockchainObject getObject() throws ORMException {
        logger.trace("START getObject()");
        BlockchainObject result = null;


        String currentAttributeName = "";
        Class currentAttributeClass = null;
        try {
            String className = jsonArray.get(0).toString();
            JSONObject jsonObject = (JSONObject) jsonArray.get(1);

            String model = "";

            if (jsonObject.containsKey("model")) {
                model = (String) jsonObject.get("model");
            } else {
                throw new ORMException("You must provide a model name to build an object");
            }

            //Class objectClass = Class.forName(className);

            CheetahClassLoader cl;

            if (CheetahWebserver.getInstance() != null) {
                cl = CheetahWebserver.getInstance().getClassLoader();
            } else {
                cl = new CheetahClassLoader(Thread.currentThread().getContextClassLoader());
            }

            Class objectClass = cl.loadClass(className);


            /*
            logger.trace("---------------------------------------");
            logger.trace("Class: " + className);
            for (Constructor constructor : objectClass.getConstructors()) {
                //            logger.debug("Constructor: " + constructor.toGenericString());
            }
            for (Method core : objectClass.getMethods()) {
                //            logger.debug("Method: " + core.toGenericString());
                //            logger.debug("Method: " + core.getName());
            }
            logger.trace("---------------------------------------");
            */

            result = (BlockchainObject) objectClass.newInstance();
            logger.trace("result.getClass(): " + result.getClass());
            result.setModel(model);

            /*
        this.parentList = new AttrIDList();
        this.createDateTime = new AttrDateTime();
             */

            for (Object systemAttributeNameObject : jsonObject.keySet()) {
                String systemAttributeName = (String) systemAttributeNameObject;
                currentAttributeName = systemAttributeName;
                if (!systemAttributeName.equals("attrMap")) {

                    Object systemAttributeValue = "";
                    Object systemAttributeValueObject = jsonObject.get(systemAttributeName);
                    Class systemAttributeType = String.class;

                    if (systemAttributeValueObject != null) {
                        if (JSONObject.class.isAssignableFrom(systemAttributeValueObject.getClass())) {
                            systemAttributeValue = (JSONObject) jsonObject.get(systemAttributeName);
                            systemAttributeType = JSONObject.class;

                        } else if (JSONArray.class.isAssignableFrom(systemAttributeValueObject.getClass())) {
                            systemAttributeValue = (JSONArray) jsonObject.get(systemAttributeName);
                            systemAttributeType = JSONArray.class;

                        } else {
                            systemAttributeValue = String.valueOf(jsonObject.get(systemAttributeName));
                        }
                    }

                    logger.trace("systemAttributeName: " + systemAttributeName + ": " + systemAttributeValue.toString());
                    systemAttributeName = systemAttributeName.substring(0, 1).toUpperCase() + systemAttributeName.substring(1, systemAttributeName.length());

                    if (!(systemAttributeType.equals(String.class) && systemAttributeValue.toString().toString().equals(""))) {
                        for (Method method : objectClass.getMethods()) {
                            if (method.getName().equals("set" + systemAttributeName)) {
                                logger.trace("Method SET : " + systemAttributeName);

                                Class[] parameterTypes = method.getParameterTypes();

                                if (parameterTypes.length > 0) {
                                    Class parameterClass = parameterTypes[0];

                                    Class[] stringArgsClass = new Class[]{systemAttributeType};
                                    Constructor stringArgsConstructor = parameterClass.getConstructor(stringArgsClass);
                                    Object[] stringArgs = new Object[]{systemAttributeValue};

                                    Object systemAttributeObject = stringArgsConstructor.newInstance(stringArgs);

                                    Object[] attrArgs = new Object[]{systemAttributeObject};
                                    method.invoke(result, attrArgs);
                                }
                            }
                        }
                    }
                }
            }

            /**
             * **** ATTRIBUTES SECTION ***********
             */
            JSONObject jsonObjectAttr = (JSONObject) ((JSONArray) jsonObject.get("attrMap")).get(1);
            setAttrMap(result, jsonObjectAttr);

            /*
            for (Object objectAttr : jsonObjectAttr.entrySet()) {
                Map.Entry<String, JSONArray> entry = (Map.Entry<String, JSONArray>) objectAttr;

                Class[] stringArgsClass = new Class[]{String.class};
                //objectClass = Class.forName(entry.getValue().get(0).toString());

                currentAttributeName = entry.getKey();
                objectClass = CheetahWebserver.getInstance().getClassLoader().loadClass(entry.getValue().get(0).toString());

                if (currentAttributeName.contains(".")) {
                    currentAttributeName = currentAttributeName.substring(currentAttributeName.lastIndexOf(".") + 1);
                }
                currentAttributeClass = objectClass;

                Constructor stringArgsConstructor = objectClass.getConstructor(stringArgsClass);

                Object[] stringArgs;
                if (entry.getValue().get(1) != null) {
                    stringArgs = new Object[]{String.valueOf(entry.getValue().get(1))};
                } else {
                    stringArgs = new Object[]{""};
                }

                Attr attr = (Attr) stringArgsConstructor.newInstance(stringArgs);
                result.put(entry.getKey(), attr);
            }
            */

        } catch (InvocationTargetException ex) {
            logger.error("Error building attribute: " + ex.toString());

            if (currentAttributeClass != null) {
                String format = "";
                Attr attr = null;
                try {
                    Constructor stringArgsConstructor = currentAttributeClass.getConstructor(String.class);
                    attr = (Attr) stringArgsConstructor.newInstance(new Object[]{""});

                    format = attr.getFormat();
                    logger.error("Attribute '" + currentAttributeName + "' format must be: " + format);

                } catch (Exception e) {
                    logger.error("Error building error message: " + ex.toString());
                    throw new ORMException("Error building error message: " + ex.toString());
                }

                if (attr != null) {
                    throw new ORMException("Attribute '" + currentAttributeName + "' format must be: " + format);
                }
            }

        } catch (ORMException | ClassNotFoundException | IllegalAccessException | IllegalArgumentException | InstantiationException | NoSuchMethodException | SecurityException ex) {
            logger.error("Error building object from JSONArray: " + ex.toString());
            throw new ORMException("Error building object from JSONArray: " + ex.toString());
        }

        logger.trace("END getObject()");
        return result;
    }


    public static void setAttrMap(BlockchainObject object, JSONObject attrMap) throws ORMException {

        /**
         * **** ATTRIBUTES SECTION ***********
         */

        String currentAttributeName = "";
        Class currentAttributeClass = null;
        try {

            for (Object objectAttr : attrMap.entrySet()) {
                Map.Entry<String, JSONArray> entry = (Map.Entry<String, JSONArray>) objectAttr;

                Class[] stringArgsClass = new Class[]{String.class};
                //objectClass = Class.forName(entry.getValue().get(0).toString());

                currentAttributeName = entry.getKey();

                CheetahClassLoader cl;

                if (CheetahWebserver.getInstance() != null) {
                    cl = CheetahWebserver.getInstance().getClassLoader();
                } else {
                    cl = new CheetahClassLoader(Thread.currentThread().getContextClassLoader());
                }

                Class objectClass = cl.loadClass(entry.getValue().get(0).toString());

                if (currentAttributeName.contains(".")) {
                    currentAttributeName = currentAttributeName.substring(currentAttributeName.lastIndexOf(".") + 1);
                }
                currentAttributeClass = objectClass;

                Constructor stringArgsConstructor = objectClass.getConstructor(stringArgsClass);

                Object[] stringArgs;
                if (entry.getValue().get(1) != null) {
                    stringArgs = new Object[]{String.valueOf(entry.getValue().get(1))};
                } else {
                    stringArgs = new Object[]{""};
                }

                Attr attr = (Attr) stringArgsConstructor.newInstance(stringArgs);
                object.put(entry.getKey(), attr);
            }

        } catch (InvocationTargetException ex) {
            logger.error("Error building attribute: " + ex.toString());

            if (currentAttributeClass != null) {
                String format = "";
                Attr attr = null;
                try {
                    Constructor stringArgsConstructor = currentAttributeClass.getConstructor(String.class);
                    attr = (Attr) stringArgsConstructor.newInstance(new Object[]{""});

                    format = attr.getFormat();
                    logger.error("Attribute '" + currentAttributeName + "' format must be: " + format);

                } catch (Exception e) {
                    logger.error("Error building error message: " + ex.toString());
                    throw new ORMException("Error building error message: " + ex.toString());
                }

                if (attr != null) {
                    throw new ORMException("Attribute '" + currentAttributeName + "' format must be: " + format);
                }
            }

        } catch (ClassNotFoundException | IllegalAccessException | IllegalArgumentException | InstantiationException | NoSuchMethodException | SecurityException ex) {
            logger.error("Error building object from JSONArray: " + ex.toString());
            throw new ORMException("Error building object from JSONArray: " + ex.toString());
        }
    }
}
