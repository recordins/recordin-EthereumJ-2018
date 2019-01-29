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

package org.cheetah.webserver.page.json;

import com.recordins.recordin.Main;
import com.recordins.recordin.orm.BlockchainObject;
import com.recordins.recordin.orm.User;
import com.recordins.recordin.orm.attribute.AttrID;
import com.recordins.recordin.orm.core.BlockchainIndex;
import com.recordins.recordin.orm.core.BlockchainObjectIndex;
import com.recordins.recordin.orm.core.BlockchainObjectReader;
import com.recordins.recordin.orm.exception.ORMException;
import org.cheetah.webserver.Page;
import org.json.simple.JSONObject;
import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.recordins.recordin.Main.sendWebsocket;

public class GetNodeStatus extends Page {

    private static Logger logger = LoggerFactory.getLogger(GetNodeStatus.class);

    @Override
    public void handle(Request request, Response response) {
        logger.debug("START: Handle Request");

        String userName = this.webserver.getUsername(request);

        String sessionID = this.webserver.getSessionID(request);

        User user = User.getUser(userName);

        user.setSessionCookie(sessionID);

        response.setValue("Content-Type", "application/json");

        JSONObject jsonResult = new JSONObject();

        JSONObject result = Main.getInstance().getNodeStatus();

        result.put("loggedUser", user.getDisplayName());
        result.put("loggedUserID", user.getId().toString());

        jsonResult.put("MessageType", "Status");
        jsonResult.put("MessageValue", result);

        //sendWebsocket(jsonResultConfig.toJSONString(), user);
                /*
                webserver.distributeToWebsocketServiceMessage("org.cheetah.webserver.page.websocket.Recordin", jsonResultConfig.toJSONString(), user.getLogin());
                */
        //    }
        //};
        //T.start();

        body.println(jsonResult);
        logger.debug(" END : Handle Request");
    }
}
