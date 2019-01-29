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

package org.cheetah.webserver.page.orm;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.recordins.recordin.orm.BlockchainObject;
import com.recordins.recordin.orm.User;
import com.recordins.recordin.orm.attribute.AttrID;
import com.recordins.recordin.orm.attribute.AttrIDList;
import com.recordins.recordin.orm.core.BlockchainIndex;
import com.recordins.recordin.orm.core.BlockchainObjectIndex;
import com.recordins.recordin.orm.core.BlockchainObjectReader;
import com.recordins.recordin.utils.DeepCopy;
import org.cheetah.webserver.Page;
import org.json.JSONException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class History extends Page {

    private static Logger logger = LoggerFactory.getLogger(History.class);

    @Override
    public void handle(Request request, Response response) {
        logger.debug("START: Handle Request");

        String userName = this.webserver.getUsername(request);

        String sessionID = this.webserver.getSessionID(request);

        User user = User.getUser(userName);

        user.setSessionCookie(sessionID);

        response.setValue("Content-Type", "application/json");

        JSONObject jsonResult = new JSONObject();
        jsonResult.put("MessageType", "Success");
        jsonResult.put("MessageValue", "[]");

        JSONArray jsonObjectsResult = new JSONArray();

        String id = request.getParameter("id");

        this.debugString.append(" -- PARAMETERS -- ").append(System.lineSeparator());
        this.debugString.append("uid         : " + id).append(System.lineSeparator());
        this.debugString.append(" -- ---------- -- ").append(System.lineSeparator());

        try {

            if (id.matches(AttrID.REGEX_PATTERN)) {
                id = id.substring(id.lastIndexOf(".") + 1);
            }

            BlockchainIndex<String, String> currentVersionsIndex = BlockchainObjectIndex.getInstance().getIndex(BlockchainObjectIndex.INDEX_TYPE.CURRENT_VERSIONS);
            String idString = currentVersionsIndex.get(id);

            /*
            for(Map.Entry e: BlockchainObjectIndex.getInstance().highestUIDTreeMap.entrySet()){

                logger.debug("highestUIDTreeMap(" + e.getKey() + "): " + e.getValue());
            }
            */


            BlockchainObjectReader reader = BlockchainObjectReader.getInstance(user);
            BlockchainObject object = reader.read(idString);
            BlockchainObject next = null;
            JSONArray currentObject = reader.readJson(object.getId());
            AttrIDList attrIdList = object.getParentList();

            if (attrIdList.size() > 0) {
                for (int i = 0; i < attrIdList.size(); i++) {

                    JSONArray nextObject = new JSONArray();

                    object = reader.read(attrIdList.get(i));

                    if (object != null) {

                        if (i < attrIdList.size() - 1) {
                            AttrID attrIDnext = attrIdList.get(i + 1);

                            nextObject = reader.readJson(attrIDnext);
                            next = reader.read(attrIDnext);
                        }

                        ObjectMapper jackson = new ObjectMapper();

                        JSONObject currentObjectSimplified = simplifyJson(currentObject);
                        JSONObject nextObjectSimplified = new JSONObject();
                        if (nextObject.size() > 0) {
                            nextObjectSimplified = simplifyJson(nextObject);
                        }

                        JsonNode afterNode = jackson.readTree(currentObjectSimplified.toJSONString());
                        JsonNode beforeNode = jackson.readTree(nextObjectSimplified.toJSONString());
                        JsonNode patchNode = com.flipkart.zjsonpatch.JsonDiff.asJson(beforeNode, afterNode);


                        String[] operations = new String[]{"replace", "add", "remove", "move", "copy"};

                        ArrayList<String> attributes = new ArrayList();

                        JSONObject objectHistory = new JSONObject();
                        JSONArray result = new JSONArray();

                        boolean created = true;
                        String date = object.getDateTimeCreate().toString();
                        AttrID userID = object.getUserCreateID();

                        if (object.getDateTimeUpdate() != null) {
                            date = object.getDateTimeUpdate().toString();
                            userID = object.getUserUpdateID();
                            created = false;
                        }

                        for (String operation : operations) {
                            for (int j = 0; j < patchNode.size(); j++) {

                                JSONObject resultNode = new JSONObject();
                                ObjectNode node = (ObjectNode) patchNode.get(j);

                                if (node.get("op").asText().equals(operation)) {
                                    String path = node.get("path").asText();
                                    path = path.substring(1);

                                    if (path.startsWith("attrMap")) {

                                        String attrMapPath = "attribute";
                                        path = path.substring(7);

                                        if (path.startsWith("/")) {
                                            path = path.substring(1);
                                        }

                                        if (path.contains("/")) {
                                            path = path.substring(0, path.indexOf("/"));
                                        }

                                        attrMapPath += "/" + path;


                                        //logger.debug("path: " + path);

                                        if (!path.equals("")) {
                                            if (!attributes.contains(attrMapPath)) {

                                                Object value = ((JSONObject) ((JSONArray) ((JSONObject) currentObject.get(1)).get("attrMap")).get(1)).get(path);
                                                String op = node.get("op").asText();

                                                String nextValue = null;
                                                if (nextObject.size() > 0) {
                                                    if (next.get(path) != null) {
                                                        nextValue = next.get(path).toString();
                                                    } else {
                                                        nextValue = "";
                                                    }
                                                } else {
                                                    nextValue = "";
                                                }

                                                if (value == null || value.toString().equals("") || value.toString().equals("null") || object.get(path).toString().equals("")) {
                                                    op = "remove";
                                                    //value = "";
                                                }

                                            /*
                                            logger.debug("path: " + attrMapPath);
                                            logger.debug("nextValue: " + nextValue.toString());
                                            logger.debug("value: " + value);
                                            */


                                                if (nextValue.equals("") && (value != null && !value.toString().equals(""))) {
                                                    op = "add";
                                                }

                                                resultNode.put("op", op);
                                                resultNode.put("path", attrMapPath);
                                                resultNode.put("value", value);

                                                result.add(resultNode);
                                                attributes.add(attrMapPath);
                                            }
                                        } else if (created) {
                                            JSONObject jsonAttrMap = ((JSONObject) ((JSONArray) ((JSONObject) currentObject.get(1)).get("attrMap")).get(1));

                                            for (Object key : jsonAttrMap.keySet()) {
                                                resultNode = new JSONObject();
                                                String keyString = String.valueOf(key);

                                                String attrMapPathTMP = "attribute/" + keyString;


                                                if (!attributes.contains(attrMapPathTMP)) {

                                                    Object value = jsonAttrMap.get(keyString);

                                                    if (value != null && !value.equals("") && !object.get(keyString).toString().equals("") && !object.get(keyString).toString().equals("[]")) {
                                                        resultNode.put("op", "add");
                                                        resultNode.put("path", attrMapPathTMP);
                                                        resultNode.put("value", jsonAttrMap.get(key));

                                                        result.add(resultNode);
                                                        attributes.add(attrMapPathTMP);
                                                    }
                                                }
                                            }
                                        }
                                    } else {

                                        String systemPath = "system";

                                        if (path.contains("/")) {
                                            path = path.substring(0, path.indexOf("/"));
                                        }

                                        systemPath += "/" + path;

                                        if (!attributes.contains(systemPath)) {

                                            Object value = ((JSONObject) currentObject.get(1)).get(path);
                                            Object nextValue = null;
                                            if (nextObject.size() > 0) {
                                                nextValue = ((JSONObject) nextObject.get(1)).get(path);
                                            }

                                            String op = node.get("op").asText();

                                            if (value == null || value.toString().equals("")) {
                                                op = "remove";
                                            }

                                            if (nextValue == null && (value != null && !value.toString().equals(""))) {
                                                op = "add";
                                            }

                                            if (!op.equals("remove") || (op.equals("remove") && !created)) {
                                                resultNode.put("op", op);
                                                resultNode.put("path", systemPath);
                                                resultNode.put("value", value);

                                                result.add(resultNode);
                                                attributes.add(systemPath);
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        date = date.replaceAll("\"", "");

                        JSONObject jsonHeader = new JSONObject();

                        jsonHeader.put("date", date);
                        jsonHeader.put("created", created);
                        if (userID != null) {
                            jsonHeader.put("user", userID.toString());
                        }

                        result = sortJson(result);
                        objectHistory.put("header", jsonHeader);
                        objectHistory.put("history", result);

                        jsonObjectsResult.add(objectHistory);
                        currentObject = nextObject;
                    }
                }
            }

            jsonResult.replace("MessageValue", jsonObjectsResult);

        } catch (Exception ex) {
            jsonResult.put("MessageType", "Error");
            jsonResult.put("MessageValue", "Error generating History for id: '" + id + "': " + ex.toString());
            logger.error("Error generating History for id: '" + id + "': " + ex.toString());
            ex.printStackTrace();
        }

        body.println(jsonResult);
        logger.debug(" END : Handle Request");
    }

    public JSONArray sortJson(JSONArray jsonArray) {
        JSONArray result = new JSONArray();

        List<JSONObject> jsonValues = new ArrayList<JSONObject>();
        for (int i = 0; i < jsonArray.size(); i++) {
            jsonValues.add((JSONObject) jsonArray.get(i));
        }
        Collections.sort(jsonValues, new Comparator<JSONObject>() {
            //You can change "Name" with "ID" if you want to sort by ID
            private static final String KEY_NAME = "path";

            @Override
            public int compare(JSONObject a, JSONObject b) {
                String valA = new String();
                String valB = new String();

                try {
                    valA = (String) a.get(KEY_NAME);
                    valB = (String) b.get(KEY_NAME);
                } catch (JSONException e) {
                    //do something
                }

                return valA.compareTo(valB);
                //if you want to change the sort order, simply use the following:
                //return -valA.compareTo(valB);
            }
        });

        for (int i = 0; i < jsonArray.size(); i++) {
            result.add((JSONObject) jsonValues.get(i));
        }

        return result;
    }

    public JSONObject simplifyJson(JSONArray jsonArray) {
        JSONObject result = new JSONObject();


        if (jsonArray != null && jsonArray.size() > 1) {
            jsonArray = (JSONArray) DeepCopy.copy(jsonArray);
            result = (JSONObject) jsonArray.get(1);

            if (result.containsKey("attrMap")) {
                JSONArray jsonAttrMap = (JSONArray) result.get("attrMap");
                result.replace("attrMap", jsonAttrMap.get(1));
            }
        }

        return result;
    }
}
