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

import com.recordins.recordin.Main;
import com.recordins.recordin.orm.BlockchainObject;
import com.recordins.recordin.orm.User;
import com.recordins.recordin.orm.exception.ORMException;
import com.recordins.recordin.orm.core.BlockchainObjectFactory;
import com.recordins.recordin.orm.core.BlockchainObjectWriter;
import org.cheetah.webserver.AbstractPageDefault;
import org.cheetah.webserver.Page;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import org.simpleframework.http.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.recordins.recordin.Main.syncComplete;

public class Write extends Page {

    private static Logger logger = LoggerFactory.getLogger(Write.class);

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
        jsonResult.put("MessageValue", "Write request transmitted");

        String model = request.getParameter("model");
        String vals = request.getParameter("vals");

        this.debugString.append(" -- PARAMETERS -- ").append(System.lineSeparator());
        this.debugString.append("model       : " + model).append(System.lineSeparator());
        this.debugString.append("vals        : " + vals).append(System.lineSeparator());
        this.debugString.append(" -- ---------- -- ").append(System.lineSeparator());

        if (!syncComplete) {
            jsonResult.put("MessageType", "Error");
            jsonResult.put("MessageValue", "Error at object writing: Sync must be complete before asking for an object write");
            body.println(jsonResult);
            return;
        }
        if (Main.initDataModelFlag || Main.initFlag) {
            jsonResult.put("MessageType", "Error");
            jsonResult.put("MessageValue", "Error at object writing: Platform init must be complete before asking for an object write");
            body.println(jsonResult);
            return;
        }

        JSONParser parser = new JSONParser();
        JSONArray jsonArray;
        BlockchainObject object = null;

        try {
            jsonArray = (JSONArray) parser.parse(vals);
            JSONObject jsonObject = (JSONObject) jsonArray.get(1);

            if (!jsonObject.containsKey("Model")) {
                jsonObject.put("Model", model);
            }

            logger.trace("OBJECT JSONArray: " + jsonArray);

            object = BlockchainObjectFactory.getInstance(jsonArray).getBlockchainObject();

            logger.trace("OBJECT toString : " + object.toString());

            String transactionID = BlockchainObjectWriter.getInstance(user).writeAsync(object);

            logger.debug("transactionID : " + transactionID);
            jsonResult.put("MessageTransactionID", transactionID);

        } catch (ORMException ex) {
            jsonResult.put("MessageType", "Error");
            jsonResult.put("MessageValue", "Error during object writing: " + ex.toString());
            logger.error("Error during object writing: " + ex.toString());
        } catch (Exception ex) {
            jsonResult.put("MessageType", "Error");
            jsonResult.put("MessageValue", "Error during object writing: " + ex.toString());
            logger.error("Error during object writing: " + ex.toString());
            ex.printStackTrace();
        }

        body.println(jsonResult);
        logger.debug(" END : Handle Request");
    }

    private void handleDefaultPage(Status status, Exception e, Request request, Response response) throws Exception {

        Class lookupPage = null;
        lookupPage = this.webserver.getDefaultPageClass();
        AbstractPageDefault pageDefault = (AbstractPageDefault) lookupPage.newInstance();
        pageDefault.setRessources(body, webserver, debugString);
        pageDefault.setStatus(status);
        pageDefault.setException(e);
        pageDefault.handle(request, response);
    }
}
