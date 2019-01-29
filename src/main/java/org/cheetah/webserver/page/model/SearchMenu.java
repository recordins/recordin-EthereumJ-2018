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

import com.recordins.recordin.orm.User;
import com.recordins.recordin.orm.core.BlockchainObjectReader;
import org.cheetah.webserver.Page;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SearchMenu extends Page {

    private static Logger logger = LoggerFactory.getLogger(SearchMenu.class);

    @Override
    public void handle(Request request, Response response) {
        logger.trace("START: Handle Request");

        String userName = this.webserver.getUsername(request);

        String sessionID = this.webserver.getSessionID(request);

        User user = User.getUser(userName);

        user.setSessionCookie(sessionID);

        response.setValue("Content-Type", "application/json");

        JSONObject jsonResult = new JSONObject();
        jsonResult.put("MessageType", "Success");
        jsonResult.put("MessageValue", "[]");
        JSONArray jsonMenuResult = new JSONArray();

        try {

            BlockchainObjectReader reader = BlockchainObjectReader.getInstance(user);

            jsonMenuResult = reader.searchMenu();
            //jsonResult.replace("MessageValue", jsonMenuResult.toJSONString());
            jsonResult.replace("MessageValue", jsonMenuResult);

        } catch (Exception ex) {
            jsonResult.put("MessageType", "Error");
            jsonResult.put("MessageValue", "Error building menu JSON: " + ex.toString());
            logger.error("Error building menu JSON: " + ex.toString());
            ex.printStackTrace();
        }

        body.println(jsonResult);
        logger.debug(" END : Handle Request");
    }
}
