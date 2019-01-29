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

package org.cheetah.webserver.page.model;

import com.recordins.recordin.orm.BlockchainObject;
import com.recordins.recordin.orm.User;
import com.recordins.recordin.orm.action.ActionDefinition;
import com.recordins.recordin.orm.attribute.AttrAttribute;
import com.recordins.recordin.orm.attribute.AttrID;
import com.recordins.recordin.orm.exception.ORMException;
import com.recordins.recordin.orm.core.BlockchainObjectIndex.INDEX_TYPE;
import com.recordins.recordin.orm.core.BlockchainObjectReader;

import static com.recordins.recordin.utils.ReflexionUtils.getClassesFromPackage;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.TreeMap;

import org.cheetah.webserver.CheetahWebserver;
import org.cheetah.webserver.Page;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JsonSchema extends Page {

    private static Logger logger = LoggerFactory.getLogger(JsonSchema.class);

    @Override
    public void handle(Request request, Response response) {
        logger.debug("START: Handle Request");

        String userName = this.webserver.getUsername(request);

        String sessionID = this.webserver.getSessionID(request);

        User user = User.getUser(userName);

        user.setSessionCookie(sessionID);

        response.setValue("Content-Type", "application/json");

        String model = request.getParameter("model");

        if (model == null) {
            logger.error("You must provide a model name to search for objects");
        }

        this.debugString.append(" -- PARAMETERS -- ").append(System.lineSeparator());
        this.debugString.append("model       : " + model).append(System.lineSeparator());
        this.debugString.append(" -- ---------- -- ").append(System.lineSeparator());

        JSONObject jsonResult = new JSONObject();
        jsonResult.put("MessageType", "Success");
        jsonResult.put("MessageValue", "");

        JSONObject jsonResultObject = new JSONObject();

        BlockchainObject blockchainObject = null;
        Exception exception = null;
        // supposing model contains and AttrID

        if (model.matches(AttrID.REGEX_PATTERN)) {
            try {
                blockchainObject = BlockchainObjectReader.getAdminInstance().read(model);
            } catch (Exception ex) {
                jsonResult.put("MessageType", "Error");
                jsonResult.put("MessageValue", "Error searching Meta model for model: '" + model + "': " + ex.toString());
                logger.error("Error searching Meta model for model: '" + model + "': " + ex.toString());
                exception = ex;
            }
        }

        if (exception == null) {
            try {
                if (blockchainObject == null) {
                    BlockchainObjectReader.SearchResult searchResult = BlockchainObjectReader.getAdminInstance().search("Model", INDEX_TYPE.VIRTUAL_TABLES_ACTIVE, false, "[[\"Name\",\"=\",\"" + model + "\"]]", -1, -1, "");
                    ;
                    ArrayList<BlockchainObject> objectList = searchResult.getBlockchainObjects();
                    //ArrayList<BlockchainObject> objectList = BlockchainObjectReader.getInstance(user).search("model", INDEX_TYPE.VIRTUAL_TABLES_ACTIVE, false, "", -1, -1, "");

                    if (objectList.size() == 0) {
                        throw new ORMException("No metamodel found");
                    }
                    if (objectList.size() > 1) {
                        for (BlockchainObject object : objectList) {
                            logger.debug("schema found for model: '" + model + "': " + object.getDisplayName() + ": " + object.getId().toString());
                        }
                        throw new ORMException("More than one metamodel found");
                    }

                    blockchainObject = objectList.get(0);
                }

                //String jsonSchema = blockchainObject.get("JSONSchema").toString();
                if (blockchainObject.getModel().equals("Model")) {

                    //JSONObject jsonObjectDefinitions = new JSONObject();
                    JSONObject jsonObjectJavaclasses = new JSONObject();
                    JSONObject jsonObjectAttributes = new JSONObject();
                    JSONObject jsonObjectModels = new JSONObject();

                    JSONArray jsonArrayClasses = new JSONArray();
                    TreeMap<String, Class> classMap = new TreeMap();

                    logger.debug("schema for model: '" + model + "'");

                    for (Class c : getClassesFromPackage("com.recordins.recordin.orm", CheetahWebserver.getInstance().getClassLoader())) {
                        if (!c.getSimpleName().equals("ACTION")) {
                            classMap.put(c.getSimpleName(), c);
                        }
                    }
                    jsonArrayClasses.addAll(classMap.keySet());

                    JSONArray jsonArrayAttributes = new JSONArray();
                    classMap = new TreeMap();
                    for (Class c : getClassesFromPackage("com.recordins.recordin.orm.attribute", CheetahWebserver.getInstance().getClassLoader())) {
                        //if (!c.getSimpleName().equals("Attr")) {
                        if (!Modifier.isAbstract(c.getModifiers()) && !AttrAttribute.class.isAssignableFrom(c)) {
                            classMap.put(c.getSimpleName(), c);
                        }
                    }
                    jsonArrayAttributes.addAll(classMap.keySet());

                    BlockchainObjectReader.SearchResult modelsList = BlockchainObjectReader.getAdminInstance().search("Model", INDEX_TYPE.VIRTUAL_TABLES_ACTIVE, false, "", -1, -1, "");

                    ArrayList<BlockchainObject> models = modelsList.getBlockchainObjects();

                    JSONArray jsonArrayModels = new JSONArray();
                    classMap = new TreeMap();
                    for (BlockchainObject object : models) {
                        if (!object.getDisplayName().equals("Model")) {
                            classMap.put(object.getDisplayName(), object.getClass());
                        }
                    }
                    jsonArrayModels.addAll(classMap.keySet());
                    jsonObjectModels.put("type", "string");
                    jsonObjectModels.put("enum", jsonArrayModels);

                    jsonObjectJavaclasses.put("type", "string");
                    jsonObjectJavaclasses.put("enum", jsonArrayClasses);

                    jsonObjectAttributes.put("type", "string");
                    jsonObjectAttributes.put("enum", jsonArrayAttributes);

                /*
                jsonObjectDefinitions.put("javaclasses", jsonObjectJavaclasses);
                jsonObjectDefinitions.put("attributes", jsonObjectAttributes);
                jsonObjectDefinitions.put("models", jsonObjectModels);
                 */
                    //jsonSchema = "{ \"definitions\": " + jsonObjectDefinitions.toJSONString() + "," + jsonSchema.substring(1);
                    jsonResultObject.put("JavaClasses", jsonArrayClasses);
                    jsonResultObject.put("Attributes", jsonArrayAttributes);
                    jsonResultObject.put("Models", jsonArrayModels);
                }

                JSONArray jsonArrayAttributeProperties = new JSONArray();

                for (Field field : AttrAttribute.class.getFields()) {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("Name", field.getName());
                    jsonObject.put("Type", field.getType().getSimpleName());
                    jsonArrayAttributeProperties.add(jsonObject);
                }

                jsonResultObject.put("AttributeProperties", jsonArrayAttributeProperties);

                String className = blockchainObject.get("Java Class").toString();

                jsonResultObject.put("ModelJavaClass", className);
                jsonResultObject.put("ModelSchema", BlockchainObject.getJSON(blockchainObject));
                jsonResultObject.put("ModelID", blockchainObject.getId().toString());

                try {
                    //Class c = Class.forName("com.recordins.recordin.orm." + className);
                    Class c = CheetahWebserver.getInstance().getClassLoader().loadClass("com.recordins.recordin.orm." + className);

                    BlockchainObject object = (BlockchainObject) c.newInstance();

                    JSONArray jsonActionlist = new JSONArray();
                    for (ActionDefinition action : object.getActionList()) {
                        jsonActionlist.add(action.getJSONArray());
                    }

                    jsonResultObject.put("ModelActionList", jsonActionlist);
                } catch (Exception ex) {
                    logger.error("Error searching action list for model: '" + model + "': " + ex.toString());
                }

                jsonResult.replace("MessageValue", jsonResultObject);

            } catch (Exception ex) {
                jsonResult.put("MessageType", "Error");
                jsonResult.put("MessageValue", "Error searching Meta model for model: '" + model + "': " + ex.toString());
                logger.error("Error searching Meta model for model: '" + model + "': " + ex.toString());
            }
        }

        body.println(jsonResult);

        logger.debug(" END : Handle Request");
    }

}
