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
import com.recordins.recordin.orm.core.BlockchainObjectReader;
import org.cheetah.webserver.Page;
import org.json.simple.JSONObject;
import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReadTree extends Page {

    private static Logger logger = LoggerFactory.getLogger(ReadTree.class);

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
        jsonResult.put("MessageValue", "{}");

        JSONObject jsonResultTree = new JSONObject();

        //String model = request.getParameter("model");
        String id = request.getParameter("id");

        this.debugString.append(" -- PARAMETERS -- ").append(System.lineSeparator());
        //this.debugString.append("model       : " + model).append(System.lineSeparator());
        this.debugString.append("id          : " + id).append(System.lineSeparator());
        this.debugString.append(" -- ---------- -- ").append(System.lineSeparator());

        try {
            jsonResultTree = BlockchainObjectReader.getInstance(user).readJsonTree(new AttrID(id));
            jsonResult.replace("MessageValue", jsonResultTree.toJSONString());

        } catch (Exception ex) {
            jsonResult.put("MessageType", "Error");
            jsonResult.put("MessageValue", "Error reading object with ID: '" + id + "': " + ex.toString());
            logger.error("Error reading object with ID: '" + id + "': " + ex.toString());
            ex.printStackTrace();
        }

        body.println(jsonResult);
        logger.debug(" END : Handle Request");
    }
}
