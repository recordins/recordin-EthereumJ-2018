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

import com.recordins.recordin.orm.User;
import com.recordins.recordin.orm.attribute.AttrID;
import com.recordins.recordin.orm.core.BlockchainIndex;
import com.recordins.recordin.orm.core.BlockchainObjectIndex;
import com.recordins.recordin.orm.core.BlockchainObjectReader;
import org.cheetah.webserver.Page;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GetCurrentVersion extends Page {

    private static Logger logger = LoggerFactory.getLogger(GetCurrentVersion.class);

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
        this.debugString.append("id          : " + id).append(System.lineSeparator());
        this.debugString.append(" -- ---------- -- ").append(System.lineSeparator());


        if (!((String) id).equals("null")) {
            try {
                BlockchainIndex<String, String> currentVersionsIndex = BlockchainObjectIndex.getInstance().getIndex(BlockchainObjectIndex.INDEX_TYPE.CURRENT_VERSIONS);
                String currentIdString = currentVersionsIndex.get(new AttrID(id).getUID());

                jsonResult.replace("MessageValue", BlockchainObjectReader.getInstance(user).readJson(new AttrID(String.valueOf(currentIdString))).toJSONString());

            } catch (Exception ex) {

                JSONObject jsonResultError = new JSONObject();
                jsonResult.put("MessageType", "Error");
                jsonResult.put("MessageValue", "Error reading object with id: " + id + ": " + ex.toString());

                //sendWebsocket(jsonResultError.toJSONString(), user);

                logger.error("Error reading object with id: " + id + ": " + ex.toString());
            }
        }

        body.println(jsonResult);
        logger.debug(" END : Handle Request");
    }
}
