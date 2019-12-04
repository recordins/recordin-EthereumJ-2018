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

import java.io.DataInputStream;
import java.util.ArrayList;

import com.recordins.recordin.orm.exception.ORMException;
import org.cheetah.webserver.AbstractPageDefault;
import org.cheetah.webserver.CheetahWebserver;
import org.cheetah.webserver.MimeType;
import org.cheetah.webserver.Page;
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

import static com.recordins.recordin.Main.sendWebsocket;

public class GetAttachment extends Page {

    private static Logger logger = LoggerFactory.getLogger(GetAttachment.class);

    @Override
    public void handle(Request request, Response response) {
        logger.debug("START: Handle Request");

        String userName = this.webserver.getUsername(request);

        String sessionID = this.webserver.getSessionID(request);


        //logger.warn("userName: " + userName);

        User user = User.getUser(userName);
        //logger.warn("User: " + user);

        user.setSessionCookie(sessionID);

        response.setValue("Content-Type", "application/json");

        JSONObject jsonResult = new JSONObject();
        jsonResult.put("MessageType", "Success");
        jsonResult.put("MessageValue", "Action request transmitted");

        String nodeId = request.getParameter("nodeID");
        String id = request.getParameter("id");
        String attributeName = request.getParameter("attributeName");

        if (nodeId == null) {
            nodeId = "";
        }

        this.debugString.append(" -- PARAMETERS -- ").append(System.lineSeparator());
        this.debugString.append("nodeid       : " + nodeId).append(System.lineSeparator());
        this.debugString.append("id           : " + id).append(System.lineSeparator());
        this.debugString.append("attributeName: " + attributeName).append(System.lineSeparator());

        this.debugString.append(" -- AUTHORIZATIONS -- ").append(System.lineSeparator());
        this.debugString.append("AttachmentSendAttachments    : " + ApplicationContext.getInstance().getBoolean("AttachmentSendAttachments")).append(System.lineSeparator());
        this.debugString.append("AttachmentSendAttachmentsAuthorizedNodes: " + ApplicationContext.getInstance().getString("AttachmentSendAttachmentsAuthorizedNodes")).append(System.lineSeparator());
        this.debugString.append("AttachmentSendGDPRObjects    : " + ApplicationContext.getInstance().getBoolean("AttachmentSendGDPRObjects")).append(System.lineSeparator());
        this.debugString.append("AttachmentSendGDPRObjectsAuthorizedNodes: " + ApplicationContext.getInstance().getString("AttachmentSendGDPRObjectsAuthorizedNodes")).append(System.lineSeparator());
        this.debugString.append(" -- ---------- -- ").append(System.lineSeparator());

        SystemProperties config = (SystemProperties) ApplicationContext.getInstance().get("config");
        String localNodeID = Hex.toHexString(config.nodeId());

        ArrayList<String> attachmentSendAttachmentsAuthorizedNodes = new ArrayList();
        String attachmentSendAttachmentsAuthorizedNodesString = ApplicationContext.getInstance().getString("AttachmentSendAttachmentsAuthorizedNodes");

        try {
            JSONParser parser = new JSONParser();
            JSONArray jsonArray = (JSONArray) parser.parse(attachmentSendAttachmentsAuthorizedNodesString);

            for (int i = 0; i < jsonArray.size(); i++) {
                attachmentSendAttachmentsAuthorizedNodes.add((String) jsonArray.get(i));
            }

        } catch (ParseException e) {
            logger.error("Error parsing AttachmentSendAttachmentsAuthorizedNodes: " + e.toString());
        }

        ArrayList<String> attachmentSendGDPRObjectsAuthorizedNodes = new ArrayList();
        String attachmentSendGDPRObjectsAuthorizedNodesString = ApplicationContext.getInstance().getString("AttachmentSendGDPRObjectsAuthorizedNodes");

        try {
            JSONParser parser = new JSONParser();
            JSONArray jsonArray = (JSONArray) parser.parse(attachmentSendGDPRObjectsAuthorizedNodesString);

            for (int i = 0; i < jsonArray.size(); i++) {
                attachmentSendGDPRObjectsAuthorizedNodes.add((String) jsonArray.get(i));
            }

        } catch (ParseException e) {
            logger.error("Error parsing AttachmentSendGDPRObjectsAuthorizedNodes: " + e.toString());
        }


        if (((!localNodeID.equals(nodeId) && !attributeName.equals("GDPRAttachment") && !ApplicationContext.getInstance().getBoolean("AttachmentSendAttachments")))
                || (attachmentSendAttachmentsAuthorizedNodes.size() > 0 && !attachmentSendAttachmentsAuthorizedNodes.contains(nodeId))) {

            Status status = Status.FORBIDDEN;
            response.setStatus(status);
            try {
                handleDefaultPage(status, new RuntimeException("Insufficient privileges"), request, response);
            } catch (Exception ex2) {
                debugString.append("Error generating :" + status.getDescription() + ": " + ex2.toString()).append(System.lineSeparator());
                logger.error("Error generating " + status.getDescription() + ": " + ex2.toString());
            }
            return;
        } else if (attributeName.equals("GDPRAttachment")) {

            boolean isPlatformUser = false;

            try {
                BlockchainObjectReader reader = BlockchainObjectReader.getAdminInstance();
                BlockchainObject GDPRobject = reader.read(id);

                if (User.class.isAssignableFrom(GDPRobject.getClass())) {
                    User userChecked = (User) GDPRobject;

                    if (userChecked.getLogin().equals("admin") || userChecked.getLogin().equals("guest")) {
                        isPlatformUser = true;
                    }
                }

            } catch (ORMException e) {
                e.printStackTrace();
            }

            if (((!localNodeID.equals(nodeId) && !isPlatformUser && (!ApplicationContext.getInstance().getBoolean("AttachmentSendGDPRObjects"))))
                    || (attachmentSendGDPRObjectsAuthorizedNodes.size() > 0 && !attachmentSendGDPRObjectsAuthorizedNodes.contains(nodeId))) {

                Status status = Status.FORBIDDEN;
                response.setStatus(status);
                try {
                    handleDefaultPage(status, new RuntimeException("Insufficient privileges"), request, response);
                } catch (Exception ex2) {
                    debugString.append("Error generating :" + status.getDescription() + ": " + ex2.toString()).append(System.lineSeparator());
                    logger.error("Error generating " + status.getDescription() + ": " + ex2.toString());
                }
                return;
            }
        }

        BlockchainObject object = null;
        try {

            BlockchainObjectReader reader = BlockchainObjectReader.getInstance(user);

            object = reader.read(id);

            if (object != null && checkAttachmentAccess(object, user)) {

                AttrAbstractAttachment attachment = null;
                if (attributeName.equals("GDPRAttachment")) {
                    if (GDPRObject.class.isAssignableFrom(object.getClass())) {
                        attachment = ((GDPRObject) object).getAttachment();
                    } else {
                        jsonResult.put("MessageType", "Error");
                        jsonResult.put("MessageValue", "Error getting attachment '" + attributeName + "': Object is not a GDPR Object");
                    }
                } else if (object.containsKey(attributeName)) {
                    try {
                        attachment = (AttrAbstractAttachment) object.get(attributeName);
                    } catch (ClassCastException ex) {
                        jsonResult.put("MessageType", "Error");
                        jsonResult.put("MessageValue", "Error getting attachment '" + attributeName + "': attribute is not an AttrAbstractAttachment");
                    }
                } else {
                    jsonResult.put("MessageType", "Error");
                    jsonResult.put("MessageValue", "Error getting attachment '" + attributeName + "': attachment not found");
                }

                if (attachment != null) {

                    String mimeType = "application/octet-stream";
                    String filename = attachment.get(0);
                    int extentionIndex = filename.lastIndexOf('.');

                    if (extentionIndex > 0) {
                        String extention = filename.substring(extentionIndex + 1);
                        mimeType = MimeType.getMimeType(extention);
                        response.setValue("Content-Type", mimeType);

                    }

                    DataInputStream in = attachment.getInputStream(new AttrID(id), attributeName, user);

                    response.setValue("Content-Disposition", "filename=" + attachment.getName());

                    if (in != null) {

                        byte[] buffer = new byte[1048576];

                        int readbytes = in.read(buffer);

                        while (readbytes != -1) {
                            body.write(buffer, 0, readbytes);
                            readbytes = in.read(buffer);
                        }
                        in.close();
                    } else {

                        Status status = Status.NOT_FOUND;
                        response.setStatus(status);
                        logger.error("Error getting attachment '" + attributeName + "'");
                        try {
                            handleDefaultPage(status, null, request, response);
                        } catch (Exception ex2) {
                            debugString.append("Error generating :" + status.getDescription() + ": " + ex2.toString()).append(System.lineSeparator());
                            logger.error("Error generating " + status.getDescription() + ": " + ex2.toString());
                        }
                    }
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
        } catch (AttrAttachmentSignatureException ex) {
            jsonResult.put("MessageType", "ErrorStay");
            jsonResult.put("MessageValue", "Error getting attachment '" + attributeName + "': " + ex.toString());

            Status status = Status.FORBIDDEN;
            response.setStatus(status);
            logger.error("Error getting attachment '" + attributeName + "': " + ex.toString());
            try {
                handleDefaultPage(status, ex, request, response);
            } catch (Exception ex2) {
                debugString.append("Error generating :" + status.getDescription() + ": " + ex2.toString()).append(System.lineSeparator());
                logger.error("Error generating " + status.getDescription() + ": " + ex2.toString());
            }
            ex.printStackTrace();

            sendWebsocket(jsonResult.toJSONString(), user);
        } catch (Exception ex) {

            if (object != null && attributeName.equals("GDPRAttachment")) {

                logger.info(" GET GDPRAttachment ");
                logger.info(" object.getModel() : " + object.getModel());
                logger.info(" object.isDeleted(): " + object.isDeleted());

                if (object.isDeleted()) {
                    Status status = Status.FOUND;
                    response.setStatus(status);
                    return;
                }
            }

            jsonResult.put("MessageType", "Error");
            jsonResult.put("MessageValue", "Error getting attachment '" + attributeName + "': " + ex.toString());

            Status status = Status.NOT_FOUND;
            response.setStatus(status);
            logger.error("Error getting attachment '" + attributeName + "': " + ex.toString());
            try {
                handleDefaultPage(status, ex, request, response);
            } catch (Exception ex2) {
                debugString.append("Error generating :" + status.getDescription() + ": " + ex2.toString()).append(System.lineSeparator());
                logger.error("Error generating " + status.getDescription() + ": " + ex2.toString());
            }
            ex.printStackTrace();
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


    private boolean checkAttachmentAccess(BlockchainObject object, User user) {
        boolean result = false;

        if (!CheetahWebserver.getInstance().isAdminUser(user.getLogin())) {
            if (ApplicationContext.getInstance().getBoolean("ACLEnableModel")) {
                Model model = null;

                if (object.getModelID() != null) {
                    try {

                        BlockchainIndex<String, String> currentVersionsIndex = BlockchainObjectIndex.getInstance().getIndex(BlockchainObjectIndex.INDEX_TYPE.CURRENT_VERSIONS);
                        String idString = currentVersionsIndex.get(object.getModelID().getUID());

                        model = (Model) BlockchainObjectReader.getAdminInstance().readWithoutCheck(new AttrID(idString));

                    } catch (Exception ex) {
                        logger.error("Error searching Meta model for modelID: '" + object.getModelID().toString() + "': " + ex.toString());
                    }
                } else {
                    logger.warn("Warning: No model ID found at ACL check for object: '" + object.getDisplayName() + "'");
                }

                if (model != null && !model.getModelACLs().isEmpty()) {
                    boolean granted = false;
                    if (ACL.isAttachmentAllGranted(object, user, model.getModelACLs())) {
                        granted = true;
                    }

                    if (!granted) {
                        if (object.isOwner(user) && ACL.isAttachmentOwnGranted(object, user, model.getModelACLs())) {
                            granted = true;
                        }
                    }

                    if (!granted) {
                        if (ApplicationContext.getInstance().getBoolean("ACLPrintDebugTraces")) {
                            logger.warn("'" + object.getDisplayName() + "': ATTACHMENT: " + user.getLogin() + " NOT granted by Model ACL (Model: " + model.getDisplayName() + ")");
                        }
                        result = false;
                    } else {
                        result = true;
                    }
                } else {
                    result = true;
                }
            }

            if (ApplicationContext.getInstance().getBoolean("ACLEnableObject")) {
                if (!result && !object.getAcl().isEmpty() && object.checkSecurity(BlockchainObject.ACTION.ATTACHMENT, user)) {
                    result = true;
                    if (ApplicationContext.getInstance().getBoolean("ACLPrintDebugTraces")) {
                        logger.warn("'" + object.getDisplayName() + "': ATTACHMENT: " + user.getLogin() + " GRANTED by Object ACL (objectID: " + object.getId() + ")");
                    }
                } else {
                    if (result && !object.checkSecurity(BlockchainObject.ACTION.ATTACHMENT, user)) {
                        result = false;
                        if (ApplicationContext.getInstance().getBoolean("ACLPrintDebugTraces")) {
                            logger.warn("'" + object.getDisplayName() + "': ATTACHMENT: " + user.getLogin() + " NOT granted by Object ACL (objectID: " + object.getId() + ")");
                        }
                    }
                }
            }
        } else {
            result = true;
        }

        return result;
    }
}
