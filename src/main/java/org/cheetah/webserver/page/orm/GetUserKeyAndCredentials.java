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

import com.recordins.recordin.ApplicationContext;
import com.recordins.recordin.orm.*;
import com.recordins.recordin.orm.attribute.AttrAbstractAttachment;
import com.recordins.recordin.orm.attribute.AttrID;
import com.recordins.recordin.orm.attribute.exception.AttrAttachmentSignatureException;
import com.recordins.recordin.orm.core.BlockchainIndex;
import com.recordins.recordin.orm.core.BlockchainObjectIndex;
import com.recordins.recordin.orm.core.BlockchainObjectReader;
import com.recordins.recordin.orm.exception.ORMException;
import com.recordins.recordin.utils.DeepCopy;
import org.cheetah.webserver.AbstractPageDefault;
import org.cheetah.webserver.CheetahWebserver;
import org.cheetah.webserver.MimeType;
import org.cheetah.webserver.Page;
import org.cheetah.webserver.authentication.BlockchainAuthenticatorFactory;
import org.cheetah.webserver.authentication.IBlockchainAuthenticator;
import org.ethereum.config.SystemProperties;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import org.simpleframework.http.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Map;

import static com.recordins.recordin.Main.sendWebsocket;

public class GetUserKeyAndCredentials extends Page {

    private static Logger logger = LoggerFactory.getLogger(GetUserKeyAndCredentials.class);

    @Override
    public void handle(Request request, Response response) {
        logger.debug("START: Handle Request");

        String userName = this.webserver.getUsername(request);

        String sessionID = this.webserver.getSessionID(request);


        //logger.warn("userName: " + userName);

        User user = User.getUser(userName);
        //logger.warn("User: " + user);

        user.setSessionCookie(sessionID);

        response.setValue("Content-Type", "application/octet-stream");

        JSONObject jsonResult = new JSONObject();
        jsonResult.put("MessageType", "Success");
        jsonResult.put("MessageValue", "Action request transmitted");

        String nodeId = request.getParameter("nodeID");
        String id = request.getParameter("id");
        String attributeName = request.getParameter("attributeName");

        if (nodeId == null) {
            nodeId = "";
        }

        if (id == null) {
            id = "";
        }

        this.debugString.append(" -- PARAMETERS -- ").append(System.lineSeparator());
        this.debugString.append("nodeid       : " + nodeId).append(System.lineSeparator());
        this.debugString.append("id           : " + id).append(System.lineSeparator());

        this.debugString.append(" -- AUTHORIZATIONS -- ").append(System.lineSeparator());
        this.debugString.append("AttachmentSendUserCredentials: " + ApplicationContext.getInstance().getBoolean("AttachmentSendUserCredentials")).append(System.lineSeparator());
        this.debugString.append("AttachmentSendUserCredentialsAuthorizedNodes: " + ApplicationContext.getInstance().getString("AttachmentSendUserCredentialsAuthorizedNodes")).append(System.lineSeparator());
        this.debugString.append(" -- ---------- -- ").append(System.lineSeparator());

        SystemProperties config = (SystemProperties) ApplicationContext.getInstance().get("config");
        String localNodeID = Hex.toHexString(config.nodeId());

        User requestedUser = null;

        if (!id.equals("")) {
            try {
                BlockchainObjectReader reader = BlockchainObjectReader.getInstance(user);
                BlockchainObject object = reader.read(id);

                if (object.getModel().equals("User")) {
                    requestedUser = (User) object;
                } else {
                    requestedUser = object.getUserOwner();
                }


            } catch (ORMException e) {
                Status status = Status.FORBIDDEN;
                response.setStatus(status);
                logger.error("Error reading user with ID: '" + id + "': " + e.toString());
                try {
                    handleDefaultPage(status, new ORMException("Error reading user with ID: '" + id + "': " + e.toString()), request, response);
                } catch (Exception ex2) {
                    debugString.append("Error generating :" + status.getDescription() + ": " + ex2.toString()).append(System.lineSeparator());
                    logger.error("Error generating " + status.getDescription() + ": " + ex2.toString());
                }
                return;
            } catch (Exception e) {
                Status status = Status.INTERNAL_SERVER_ERROR;
                response.setStatus(status);
                logger.error("Error reading user with ID: '" + id + "': " + e.toString());
                try {
                    handleDefaultPage(status, new ORMException("Error reading user with ID: '" + id + "': " + e.toString()), request, response);
                } catch (Exception ex2) {
                    debugString.append("Error generating :" + status.getDescription() + ": " + ex2.toString()).append(System.lineSeparator());
                    logger.error("Error generating " + status.getDescription() + ": " + ex2.toString());
                }
                return;
            }
        }


        if (requestedUser != null) {

            ArrayList<String> attachmentSendUserCredentialsAuthorizedNodes = new ArrayList();
            String attachmentSendUserCredentialsAuthorizedNodesString = ApplicationContext.getInstance().getString("AttachmentSendUserCredentialsAuthorizedNodes");

            try {
                JSONParser parser = new JSONParser();
                JSONArray jsonArray = (JSONArray) parser.parse(attachmentSendUserCredentialsAuthorizedNodesString);

                for (int i = 0; i < jsonArray.size(); i++) {
                    attachmentSendUserCredentialsAuthorizedNodes.add((String) jsonArray.get(i));
                }

            } catch (ParseException e) {
                logger.error("Error parsing AttachmentSendUserCredentialsAuthorizedNodes: " + e.toString());
            }

            IBlockchainAuthenticator authenticator = BlockchainAuthenticatorFactory.getBlockchainAuthenticator();

            String key = authenticator.getPrivateKey(requestedUser.getLogin());
            String password = null;

            if (requestedUser.getLogin().equals("admin") || requestedUser.getLogin().equals("guest")) {
                password = authenticator.getPassword(requestedUser.getLogin());
            } else if ((ApplicationContext.getInstance().getBoolean("AttachmentSendUserCredentials") && attachmentSendUserCredentialsAuthorizedNodes.size() == 0)) {
                password = authenticator.getPassword(requestedUser.getLogin());
            } else if ((ApplicationContext.getInstance().getBoolean("AttachmentSendUserCredentials") && attachmentSendUserCredentialsAuthorizedNodes.size() > 0 && attachmentSendUserCredentialsAuthorizedNodes.contains(nodeId))) {
                password = authenticator.getPassword(requestedUser.getLogin());
            }

            AbstractMap.SimpleEntry<String, String> entry = new AbstractMap.SimpleEntry(key, password);

            try {
                body.write(DeepCopy.serialize(entry));
            } catch (IOException e) {
                logger.error("Error sending User Key And Credentials: " + e.toString());
            }

        } else {
            Status status = Status.FORBIDDEN;
            response.setStatus(status);
            try {
                handleDefaultPage(status, new RuntimeException("Insufficient privileges"), request, response);
            } catch (Exception ex2) {
                debugString.append("Error generating :" + status.getDescription() + ": " + ex2.toString()).append(System.lineSeparator());
                logger.error("Error generating " + status.getDescription() + ": " + ex2.toString());
            }
        }

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
