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

import com.recordins.recordin.orm.BlockchainObject;
import com.recordins.recordin.orm.User;
import com.recordins.recordin.orm.core.BlockchainObjectIndex.INDEX_TYPE;
import com.recordins.recordin.orm.core.BlockchainObjectReader;
import com.recordins.recordin.orm.core.BlockchainObjectWriter;

import java.util.ArrayList;

import org.cheetah.webserver.Page;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Search extends Page {

    private static Logger logger = LoggerFactory.getLogger(Search.class);

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
        JSONObject jsonSearchResult = new JSONObject();

        String menuID = request.getParameter("menuID");
        String model = request.getParameter("model");
        String index = request.getParameter("index");
        String filter = request.getParameter("filter");

        String oderParam = request.getParameter("order");
        String oldVersionParam = request.getParameter("oldVersion");

        int offset = -1;
        int limit = -1;
        //String order = "Name";
        String order = "";
        boolean oldVersion = false;

        INDEX_TYPE indexType = INDEX_TYPE.VIRTUAL_TABLES_ACTIVE;

        if (menuID == null) {
            menuID = "";
        }

        if (model == null) {
            logger.error("You must provide a model name to search for objects");
        }

        if (index == null || index.equals("")) {
            index = "active";
        }

        if (oderParam != null) {
            order = oderParam;
        }

        if (oldVersionParam != null) {
            try {
                oldVersion = Boolean.parseBoolean(oldVersionParam);
            } catch (Exception e) {
            }
        }

        try {
            offset = Integer.parseInt(request.getParameter("offset"));
        } catch (Exception e) {
        }

        try {
            limit = Integer.parseInt(request.getParameter("limit"));
        } catch (Exception e) {
        }

        try {
            limit = Integer.parseInt(request.getParameter("limit"));
        } catch (Exception e) {
        }

        if (filter == null) {
            filter = "";
        }

        this.debugString.append(" -- PARAMETERS -- ").append(System.lineSeparator());
        this.debugString.append("menuID      : " + menuID).append(System.lineSeparator());
        this.debugString.append("model       : " + model).append(System.lineSeparator());
        this.debugString.append("index       : " + index).append(System.lineSeparator());
        this.debugString.append("oldVersion  : " + oldVersion).append(System.lineSeparator());
        this.debugString.append("filter      : " + filter).append(System.lineSeparator());
        this.debugString.append("limit       : " + limit).append(System.lineSeparator());
        this.debugString.append("offset      : " + offset).append(System.lineSeparator());
        this.debugString.append("order       : " + order).append(System.lineSeparator());
        this.debugString.append(" -- ---------- -- ").append(System.lineSeparator());

        indexType = INDEX_TYPE.getIndex(index);

        try {
            // add restrict for domain search from menu object
            BlockchainObjectReader.SearchResult searchResult = BlockchainObjectReader.getInstance(user).search(model, indexType, oldVersion, filter, limit, offset, order);

            Long count = searchResult.getCount();
            Long countOld = searchResult.getCountOld();
            ArrayList<BlockchainObject> objectList = searchResult.getBlockchainObjects();

            logger.debug("Returned Count: " + objectList.size());
            logger.debug("Total    Count: " + count);
            logger.debug("Total CountOld: " + countOld);

            BlockchainObjectWriter writer = BlockchainObjectWriter.getInstance(user);

            JSONArray jsonArrayResult = new JSONArray();

            if (oldVersion) {
                String previousUID = "";
                for (BlockchainObject object : objectList) {
                    String objectUID = object.getUid();

                    JSONArray jsonArray = BlockchainObject.getJSON(object);

                    if (objectUID.equals(previousUID)) {
                        JSONArray jsonArrayOldVersion = new JSONArray();
                        JSONObject jsonObjectOldVersion = new JSONObject();
                        jsonObjectOldVersion.put("stringValue", "true");

                        jsonArrayOldVersion.add("AttrBoolean");
                        jsonArrayOldVersion.add(jsonObjectOldVersion);

                        ((JSONObject) jsonArray.get(1)).put("oldVersion", jsonArrayOldVersion);
                    }

                    //((JSONObject)jsonArray.get(1)).put("id", object.getId().longValue());
                    jsonArrayResult.add(jsonArray);
                    previousUID = object.getUid();
                }

            } else {

                for (BlockchainObject object : objectList) {
                    JSONArray jsonArray = BlockchainObject.getJSON(object);

                    jsonArrayResult.add(jsonArray);
                }
            }

            jsonSearchResult.put("BlockchainObjects", jsonArrayResult);
            jsonSearchResult.put("Count", count);
            jsonSearchResult.put("CountOld", countOld);

            jsonResult.replace("MessageValue", jsonSearchResult);

        } catch (Exception ex) {
            jsonResult.put("MessageType", "Error");
            jsonResult.put("MessageValue", "Error searching objects for model: '" + model + "'");
            logger.error("Error searching objects for model: '" + model + "'");
            ex.printStackTrace();
        }

        body.println(jsonResult);
        logger.debug(" END : Handle Request");
    }
}
