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

package com.recordins.recordin.orm.attribute;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.recordins.recordin.ApplicationContext;
import com.recordins.recordin.Main;
import com.recordins.recordin.orm.BlockchainObject;
import com.recordins.recordin.orm.GDPRObject;
import com.recordins.recordin.orm.User;
import com.recordins.recordin.orm.attribute.exception.AttrComparisonTypeException;
import com.recordins.recordin.orm.attribute.exception.AttrException;
import com.recordins.recordin.orm.exception.ORMException;
import com.recordins.recordin.orm.core.AttachmentStore;
import com.recordins.recordin.orm.core.BlockchainIndex;
import com.recordins.recordin.orm.core.BlockchainObjectIndex;
import com.recordins.recordin.utils.DateFormat;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

import com.recordins.recordin.orm.core.BlockchainObjectReader;
import org.cheetah.webserver.CheetahWebserver;
import org.cheetah.webserver.resources.upload.DownloadClient;
import org.ethereum.config.SystemProperties;
import org.ethereum.facade.Ethereum;
import org.ethereum.net.server.Channel;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;

public final class AttrAttachment extends AttrAbstractAttachment {

    /* Logger for console output */
    private static final Logger logger = LoggerFactory.getLogger(AttrAttachment.class);

    private String originNodeId = "";
    private String uid = "";

    private static ArrayList<String> readObjects = new ArrayList();

    /**
     * Initializes an instance object for {@code AttrDocument}
     */
    public AttrAttachment() throws AttrException {
        super();
        logger.trace("START AttrAttachment()");

        this.add("");

        logger.trace("END AttrAttachment()");
    }

    public AttrAttachment(String name, String location, String signature, String signerID, String originNodeId) throws AttrException {
        super(name, location, signature, signerID);
        logger.trace("START AttrAttachment(String, String, String, String, String)");

        this.add(originNodeId);

        logger.trace("END AttrAttachment()");
    }

    /**
     * Initializes an instance object for {@code AttrDocument} value
     * representation.
     *
     * @param jsonArray {@code String} representation of an {@link AttrAttachment}
     */
    public AttrAttachment(JSONArray jsonArray) throws AttrException {
        logger.trace("START AttrAttachment(JSONArray)");

        stringList.clear();
        for (int i = 0; i < jsonArray.size(); i++) {
            stringList.add((String) jsonArray.get(i));
        }

        if (jsonArray.size() > 0) {
            this.name = (String) jsonArray.get(0);
        }

        if (jsonArray.size() > 1) {
            this.location = (String) jsonArray.get(1);
        }

        if (jsonArray.size() > 2) {
            this.signature = (String) jsonArray.get(2);
        }

        if (jsonArray.size() > 3) {
            this.signerID = (String) jsonArray.get(3);
        }

        if (jsonArray.size() > 4) {
            this.originNodeId = (String) jsonArray.get(4);
        }

        logger.trace("END AttrAttachment()");
    }

    /**
     * Initializes an instance object for {@code AttrPlatformVersion} value
     * representation.
     *
     * @param value {@code String} representation of an
     *              {@link AttrPlatformVersion}
     */
    public AttrAttachment(String value) throws AttrException {
        logger.trace("START AttrAttachment(String)");

        JSONParser parser = new JSONParser();
        try {
            JSONArray jsonArray = (JSONArray) parser.parse(value);

            stringList.clear();
            for (int i = 0; i < jsonArray.size(); i++) {
                stringList.add((String) jsonArray.get(i));
            }

            if (jsonArray.size() > 0) {
                this.name = (String) jsonArray.get(0);
            }

            if (jsonArray.size() > 1) {
                this.location = (String) jsonArray.get(1);
            }

            if (jsonArray.size() > 2) {
                this.signature = (String) jsonArray.get(2);
            }

            if (jsonArray.size() > 3) {
                this.signerID = (String) jsonArray.get(3);
            }

            if (jsonArray.size() > 4) {
                this.originNodeId = (String) jsonArray.get(4);
            }

        } catch (ParseException ex) {
            logger.error("Error parsing JSONArray for AttrAttachment: " + ex.toString());
        }

        logger.trace("END AttrAttachment()");
    }

    @Override
    @JsonIgnore
    public String getFormat() {

        return "json array: [\"name\",\"location\",\"signature\",\"signerId\",\"originNodeId\"]";
    }

    @Override
    public int compareTo(Attr attr) {
        logger.trace("START compareTo(AttrAttachment)");

        if (!attr.getClass().isAssignableFrom(this.getClass())) {
            throw new AttrComparisonTypeException(attr.getClass(), this.getClass());
        }

        logger.trace("END compareTo()");
        return this.toString().compareTo(attr.toString());
    }

    @Override
    public boolean equals(Attr attr) {
        logger.trace("START equals(AttrAttachment)");

        if (!attr.getClass().isAssignableFrom(this.getClass())) {
            throw new AttrComparisonTypeException(attr.getClass(), this.getClass());
        }

        logger.trace("END equals()");
        return this.toString().equals(((AttrString) attr).toString());
    }

    public static byte[] readGDPRAttachment(JSONArray jsonArray, AttrID id, User user) {
        logger.trace("START readGDPRAttachment(JSONArray, AttrID, User)");
        byte[] result = new byte[0];

        JSONArray jsonArrayAttachment = (JSONArray) ((JSONObject) jsonArray.get(1)).get("attachment");
        AttrAttachment attrAttachment = null;
        try {
            attrAttachment = new AttrAttachment(jsonArrayAttachment);
            //attrAttachment.setUid((String) ((JSONObject) jsonArray.get(1)).get("uid"));
        } catch (AttrException ex) {
            logger.error("Error reading Attachment: " + ex.toString());
        }

        if (attrAttachment != null) {
            result = attrAttachment.readAttachment(id, "GDPRAttachment", user);
        }

        logger.trace("END readGDPRAttachment()");

        return result;
    }

    @Override
    public DataInputStream getInputStream(AttrID id, String attributeName, User user) throws IOException, ORMException {
        logger.trace("START getInputStream()");
        DataInputStream result = null;

        String attachmentName = "";
        String attachmentLocation = "";
        String attachmentSignature = "";
        String attachmentSignerId = "";
        String attachmentOriginNodeId = "";

        Path filePath = null;

        if (this.size() > 0) {
            attachmentName = (String) this.stringList.get(0);
        }
        if (this.size() > 1) {
            attachmentLocation = (String) this.stringList.get(1);
        }
        if (this.size() > 2) {
            attachmentSignature = (String) this.stringList.get(2);
        }
        if (this.size() > 3) {
            attachmentSignerId = (String) this.stringList.get(3);
        }
        if (this.size() > 4) {
            attachmentOriginNodeId = (String) this.stringList.get(4);
        }

        if (!attachmentLocation.equals("")) {

            Path rootPath = AttachmentStore.getInstance().getRootPath();
            filePath = rootPath.resolve(attachmentLocation);

            if (!Files.exists(filePath)) {
                readAttachment(id, attributeName, user);
            }

            byte[] content = getAttachmentRowContent();
            verifySignature(id, content, user, attachmentSignature, attachmentLocation);

            result = new DataInputStream(new FileInputStream(filePath.toFile()));
        }

        logger.trace("END getInputStream()");
        return result;
    }

    @Override
    public byte[] readAttachment(AttrID id, String attributeName, User user) {
        logger.trace("START readAttachment(AttrID, attributeName, User)");

        byte[] result = new byte[0];

        String attachmentName = "";
        String attachmentLocation = "";
        String attachmentSignature = "";
        String attachmentSignerId = "";
        String attachmentOriginNodeId = "";

        Path filePath = null;

        try {
            if (this.size() > 0) {
                attachmentName = (String) this.stringList.get(0);
            }
            if (this.size() > 1) {
                attachmentLocation = (String) this.stringList.get(1);
            }
            if (this.size() > 2) {
                attachmentSignature = (String) this.stringList.get(2);
            }
            if (this.size() > 3) {
                attachmentSignerId = (String) this.stringList.get(3);
            }
            if (this.size() > 4) {
                attachmentOriginNodeId = (String) this.stringList.get(4);
            }

            if (!attachmentLocation.equals("")) {


                Path rootPath = AttachmentStore.getInstance().getRootPath();
                filePath = rootPath.resolve(attachmentLocation);

                result = getAttachmentRowContent();
                verifySignature(id, result, user, attachmentSignature, attachmentLocation);
            }

        } catch (IOException ex) {

            SystemProperties config = (SystemProperties) ApplicationContext.getInstance().get("config");
            String localNodeID = Hex.toHexString(config.nodeId());


            AttrDateTime deleteDateTime = null;

            try {
                JSONArray jsonArray = BlockchainObjectReader.getInstance(User.getAdminUser(false)).readJson(id, false);
                boolean retrievePlatformUser = false;

                if (((JSONObject) jsonArray.get(1)).containsKey("model") && ((JSONObject) jsonArray.get(1)).get("model") != null) {
                    String model = (String) ((JSONObject) jsonArray.get(1)).get("model");
                    if (model.equals("User")) {
                        if (((JSONObject) jsonArray.get(1)).containsKey("login") && ((JSONObject) jsonArray.get(1)).get("login") != null) {
                            String login = (String) ((JSONObject) jsonArray.get(1)).get("login");
                            if (login != null && (login.equals("admin") || login.equals("guest"))) {
                                retrievePlatformUser = true;
                            }
                        }
                    }
                }

                if ((retrievePlatformUser || ApplicationContext.getInstance().getBoolean("AttachmentRetrieveFromExternal")) && !originNodeId.equals(localNodeID)) {

                    BlockchainIndex<String, String> currentVersionsIndex = BlockchainObjectIndex.getInstance().getIndex(BlockchainObjectIndex.INDEX_TYPE.CURRENT_VERSIONS);
                    if (currentVersionsIndex.containsKey(id.getUID())) {
                        String idString = currentVersionsIndex.get(id.getUID());

                        if (!readObjects.contains(idString)) {
                            readObjects.add(idString);
                            BlockchainObject object = BlockchainObjectReader.getAdminInstance().read(idString);
                            if (object.getDateTimeDelete() != null) {
                                deleteDateTime = object.getDateTimeDelete();
                            }
                        }
                    }

                    if (deleteDateTime == null) {

                        //if (!attachmentLocation.equals("") && !attachmentOriginNodeId.equals("") && filePath != null) {
                        if (!attachmentLocation.equals("") && filePath != null) {

                            Ethereum ethereum = (Ethereum) ApplicationContext.getInstance().get("ethereum");
                            int targetPort = CheetahWebserver.getInstance().getNetworkPort();

                            String portString = "";

                            if (targetPort != 80 && targetPort != 443) {
                                portString = ":" + targetPort;
                            }

                            for (Channel channel : Main.activePeers) {
                                /*
                                if (channel.isDisconnected()) {
                                    if(channel.isDisconnected()){
                                        //Main.activePeers.remove(channel);
                                    }
                                    continue;
                                }else {
                                */

                                Exception exception1 = null;
                                try {
                                    String URLString = "https://" + channel.getInetSocketAddress().getHostString() + portString;

                                    URLString = URLString + "/orm/GetAttachment?nodeID=" + localNodeID + "&id=" + id.toString() + "&attributeName=" + attributeName;

                                    URL url = new URL(URLString);

                                    logger.debug("Retrieving Attachment from another node: " + url.toString());

                                    if (!Files.exists(filePath.getParent())) {
                                        Files.createDirectories(filePath.getParent());
                                    }

                                    DownloadClient downloadClient = new DownloadClient(filePath, url, user.getLogin(), user.getPassword());
                                    downloadClient.setTimeout(ApplicationContext.getInstance().getInteger("AttachmentRetrieveDownloadTimeout") * 1000);
                                    downloadClient.setSSLEnforceValidation(CheetahWebserver.getInstance().isNetworkSecureSSLEnforceValidation());
                                    downloadClient.download();

                                    result = getAttachmentRowContent();
                                    logger.debug("Retrieving Attachment from another node: success");

                                } catch (Exception ex1) {

                                    exception1 = ex1;
                                    try {
                                        String URLString = "http://" + channel.getInetSocketAddress().getHostString() + portString;

                                        URLString = URLString + "/orm/GetAttachment?nodeID=" + localNodeID + "&id=" + id.toString() + "&attributeName=" + attributeName;

                                        URL url = new URL(URLString);

                                        logger.debug("Retrieving Attachment from another node: " + url.toString());

                                        if (!Files.exists(filePath.getParent())) {
                                            Files.createDirectories(filePath.getParent());
                                        }

                                        DownloadClient downloadClient = new DownloadClient(filePath, url, user.getLogin(), user.getPassword());
                                        downloadClient.setTimeout(ApplicationContext.getInstance().getInteger("AttachmentRetrieveDownloadTimeout") * 1000);
                                        downloadClient.setSSLEnforceValidation(CheetahWebserver.getInstance().isNetworkSecureSSLEnforceValidation());
                                        downloadClient.download();

                                        result = getAttachmentRowContent();
                                        logger.debug("Retrieving Attachment from another node: success");

                                    } catch (Exception ex2) {
                                        logger.error("Error retrieving Attachment from another node: " + ex2.toString());
                                        ex2.printStackTrace();
                                    }
                                }
                            }
                        } else {
                            logger.error("Error retrieving Attachment: not enough information for download: " + ex.toString());
                        }
                    }
                }
            } catch (Exception e) {
                logger.error("Error retrieving Attachment: " + e.toString());
            }

        } catch (ORMException ex) {
            logger.error("Error retrieving Attachment: " + ex.toString());
        }

        logger.trace("END readAttachment()");
        return result;
    }

    @Override
    public AttrAttachment writeAttachment(AttrAbstractAttachment attrAbstractAttachment, AttrDateTime time, String uid, User user) throws ORMException {
        AttrAttachment result = (AttrAttachment) attrAbstractAttachment;

        String filename = "";
        if (result.size() > 1) {
            filename = result.get(0).substring(18);
        }

        if (!filename.equals("")) {
            Path rootPath = AttachmentStore.getInstance().getRootPath();
            Path uidFolder = AttachmentStore.getInstance().getStorePath(uid);
            String filePathString = DateFormat.LongtoStorageDate(time.longValue()) + "-" + result.get(1) + "-" + filename;
            Path filePath = uidFolder.resolve(filePathString);

            //public AttrAttachment(String name, String location, String originNodeId, String signature) throws AttrException {
            String originNode = "";

            SystemProperties config = (SystemProperties) ApplicationContext.getInstance().get("config");
            originNode = Hex.toHexString(config.nodeId());


            /*
            if (CheetahWebserver.getInstance() != null) {
                originNode = CheetahWebserver.getInstance().URLHTTP;
            }
            */

            Path tempPath = AttachmentStore.getInstance().getTempPath().resolve(result.get(0));

            String signatureString = getSignature(user);


/*
            String signatureString = "";
            try {
                byte[] datahash = HashUtil.sha3(getContent(tempPath.toString()));
                ECKey key = ECKey.fromPrivate(Hex.decode(user.getPrivateKey().toString()));
                ECKey.ECDSASignature signature = key.sign(datahash);


                signatureString = Hex.toHexString(DeepCopy.serialize(signature));

                //logger.info("WRITE signatureString : " + signatureString);
            } catch (Exception ex) {
                throw new ORMException("Error generating attachment signature: " + ex.toString());
            }
            */

            try {
                Files.move(tempPath, filePath);
                filePath = rootPath.relativize(filePath);

                result = new AttrAttachment(filename, filePath.toString(), signatureString, user.getId().toString(), originNode);

            } catch (IOException ex) {
                throw new ORMException("Error moving temporary file: " + ex.toString());
            }

        }

        return result;
    }


    @JsonIgnore
    @Override
    protected byte[] getAttachmentRowContent() throws IOException, ORMException {

        Path path = AttachmentStore.getInstance().getTempPath().resolve(this.stringList.get(0));
        if (!Files.exists(path)) {
            String attachmentLocation = (String) this.stringList.get(1);

            Path rootPath = AttachmentStore.getInstance().getRootPath();
            path = rootPath.resolve(attachmentLocation);
        }

        return Files.readAllBytes(path);
    }

    /*
    @JsonIgnore
    public String getUid() {
        return uid;
    }

    @JsonIgnore
    public void setUid(String uid) {
        this.uid = uid;
    }
    */
}
