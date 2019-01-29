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
import com.recordins.recordin.orm.User;
import com.recordins.recordin.orm.attribute.exception.AttrAttachmentSignatureException;
import com.recordins.recordin.orm.attribute.exception.AttrException;
import com.recordins.recordin.orm.exception.ORMException;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.*;

import com.recordins.recordin.orm.core.BlockchainObjectReader;
import com.recordins.recordin.utils.DeepCopy;
import org.cheetah.webserver.authentication.BlockchainAuthenticatorFactory;
import org.cheetah.webserver.authentication.IBlockchainAuthenticator;
import org.ethereum.crypto.ECKey;
import org.ethereum.crypto.HashUtil;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;

import static com.recordins.recordin.Main.sendWebsocket;

public abstract class AttrAbstractAttachment implements List<String>, Attr {

    /* Logger for console output */
    private static final Logger logger = LoggerFactory.getLogger(AttrAbstractAttachment.class);

    protected String name = "";
    protected String location = "";
    protected String signature = "";
    protected String signerID = "";

    protected List<String> stringList = Collections.synchronizedList(new ArrayList());

    public abstract DataInputStream getInputStream(AttrID id, String attributeName, User user) throws IOException, ORMException;

    public abstract byte[] readAttachment(AttrID id, String attributeName, User user);

    protected abstract byte[] getAttachmentRowContent() throws IOException, ORMException;

    public abstract AttrAbstractAttachment writeAttachment(AttrAbstractAttachment attrAbstractAttachment, AttrDateTime time, String uid, User user) throws ORMException;

    /**
     * Initializes an instance object for {@code AttrDocument}
     */
    protected AttrAbstractAttachment() throws AttrException {
        logger.trace("START AttrAbstractAttachment()");

        stringList.add(name);
        stringList.add(location);
        stringList.add(signature);
        stringList.add(signerID);

        logger.trace("END AttrAbstractAttachment()");
    }

    protected AttrAbstractAttachment(String name, String location, String signature, String signerID) throws AttrException {
        logger.trace("START AttrAbstractAttachment(String, String, String)");

        this.name = name;
        this.location = location;
        this.signature = signature;
        this.signerID = signerID;

        stringList.add(name);
        stringList.add(location);
        stringList.add(signature);
        stringList.add(signerID);

        logger.trace("END AttrAbstractAttachment()");
    }

    /**
     * Initializes an instance object for {@code AttrAbstractAttachment} value
     * representation.
     *
     * @param jsonArray {@code JSONArray} representation of an {@link AttrAbstractAttachment}
     */
    protected AttrAbstractAttachment(JSONArray jsonArray) {
        logger.trace("START AttrAbstractAttachment(JSONArray)");

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

        logger.trace("END AttrAbstractAttachment()");
    }

    /**
     * Initializes an instance object for {@code AttrPlatformVersion} value
     * representation.
     *
     * @param value {@code String} representation of an
     *              {@link AttrAbstractAttachment}
     */
    protected AttrAbstractAttachment(String value) {
        logger.trace("START AttrAbstractAttachment(String)");

        JSONParser parser = new JSONParser();
        try {
            JSONArray jsonArray = (JSONArray) parser.parse(value);

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

        } catch (ParseException ex) {
            logger.error("Error parsing JSONArray for AttrAbstractAttachment: " + ex.toString());
        }

        logger.trace("END AttrAbstractAttachment()");
    }

    public static String getSignature(byte[] content, User user) {

        byte[] datahash = HashUtil.sha3(content);
        ECKey key = ECKey.fromPrivate(Hex.decode(user.getPrivateKey().toString()));

        ECKey.ECDSASignature signature = key.sign(datahash);

        return Hex.toHexString(DeepCopy.serialize(signature));
    }

    @JsonIgnore
    protected String getSignature(User user) throws ORMException {


        String result = "";
        try {

            byte[] content = getAttachmentRowContent();

            result = getSignature(content, user);

        } catch (Exception ex) {
            throw new ORMException("Error generating attachment signature: " + ex.toString());
        }

        return result;
    }

    protected void verifySignature(AttrID id, byte[] content, User user, String attachmentSignature, String attachmentLocation) throws ORMException {

        if (content == null || content.length == 0) {
            return;
        }

        JSONArray jsonArray = BlockchainObjectReader.getInstance(user).readJson(id, false);

        String userSignerID = this.signerID;

        if (!userSignerID.equals("") && !attachmentSignature.equals("")) {
            User userSigner = null;

            if (userSignerID.equals("1.init.init")) {
                userSigner = User.getAdminUser();

                IBlockchainAuthenticator authenticator = BlockchainAuthenticatorFactory.getBlockchainAuthenticator();
                userSigner.setPrivateKey(new AttrString(authenticator.getPrivateKey("admin")));

            } else {
                userSigner = User.getUser(new AttrID(userSignerID));
            }

            if (userSigner != null) {

                byte[] datahash = HashUtil.sha3(content);

                ECKey key = ECKey.fromPrivate(Hex.decode(userSigner.getPrivateKey().toString()));

                ECKey.ECDSASignature signature = (ECKey.ECDSASignature) DeepCopy.unserialize(Hex.decode(attachmentSignature));

                boolean verify = key.verify(datahash, signature);

                if (!verify) {

                    switch (ApplicationContext.getInstance().getString("AttachmentSignatureFailPolicy").toLowerCase()) {

                        case "error":
                            logger.error("Error validating signature of file: '" + attachmentLocation + "'");
                            throw new AttrAttachmentSignatureException("Error validating signature of file: '" + attachmentLocation + "'");

                        case "none":
                            logger.debug("Error validating signature of file: '" + attachmentLocation + "'");
                            break;

                        default:
                            logger.error("Error validating signature of file: '" + attachmentLocation + "'");

                            JSONObject jsonResult = new JSONObject();
                            jsonResult.put("MessageType", "WarningStay");
                            jsonResult.put("MessageValue", "Error validating signature of file: '" + attachmentLocation + "'");
                            sendWebsocket(jsonResult.toJSONString(), user);
                    }
                } else {
                    logger.debug("Signature verified for file: '" + attachmentLocation + "'");
                }
            } else {

                switch (ApplicationContext.getInstance().getString("AttachmentSignatureFailPolicy").toLowerCase()) {

                    case "error":
                        logger.error("Error validating signature of file: '" + attachmentLocation + "': Unable to find user key");
                        throw new AttrAttachmentSignatureException("Error validating signature of file: '" + attachmentLocation + "': Unable to find user key");

                    case "none":
                        logger.debug("Error validating signature of file: '" + attachmentLocation + "': Unable to find user key");
                        break;

                    default:
                        logger.error("Error validating signature of file: '" + attachmentLocation + "': Unable to find user key");

                        JSONObject jsonResult = new JSONObject();
                        jsonResult.put("MessageType", "WarningStay");
                        jsonResult.put("MessageValue", "Error validating signature of file: '" + attachmentLocation + "': Unable to find user key");
                        sendWebsocket(jsonResult.toJSONString(), user);
                }
            }

        } else {
            if (attachmentSignature.equals("")) {

                switch (ApplicationContext.getInstance().getString("AttachmentSignatureFailPolicy").toLowerCase()) {

                    case "error":
                        logger.error("Error validating signature of file: '" + attachmentLocation + "': Unable to find signature");
                        throw new AttrAttachmentSignatureException("Error validating signature of file: '" + attachmentLocation + "': Unable to find signature");

                    case "none":
                        logger.debug("Error validating signature of file: '" + attachmentLocation + "': Unable to find signature");
                        break;

                    default:
                        logger.error("Error validating signature of file: '" + attachmentLocation + "': Unable to find signature");

                        JSONObject jsonResult = new JSONObject();
                        jsonResult.put("MessageType", "WarningStay");
                        jsonResult.put("MessageValue", "Error validating signature of file: '" + attachmentLocation + "': Unable to find signature");
                        sendWebsocket(jsonResult.toJSONString(), user);
                }
            } else {

                switch (ApplicationContext.getInstance().getString("AttachmentSignatureFailPolicy").toLowerCase()) {

                    case "error":
                        logger.error("Error validating signature of file: '" + attachmentLocation + "': Unable to find user");
                        throw new AttrAttachmentSignatureException("Error validating signature of file: '" + attachmentLocation + "': Unable to find user");

                    case "none":
                        logger.debug("Error validating signature of file: '" + attachmentLocation + "': Unable to find user");
                        break;

                    default:
                        logger.error("Error validating signature of file: '" + attachmentLocation + "': Unable to find user");

                        JSONObject jsonResult = new JSONObject();
                        jsonResult.put("MessageType", "WarningStay");
                        jsonResult.put("MessageValue", "Error validating signature of file: '" + attachmentLocation + "': Unable to find user");
                        sendWebsocket(jsonResult.toJSONString(), user);
                }
            }
        }
    }


    /**
     * Returns {@code String} representation of current instance.
     *
     * @return {@code String} representation of current instance
     */
    @Override
    public String toString() {
        logger.trace("START toString()");

        StringBuilder string = new StringBuilder();
        string.append("[\"");
        int counter = 0;
        for (String value : this.stringList) {
            counter++;
            string.append(value);

            if (counter < this.stringList.size()) {
                string.append("\",\"");
            }
        }
        string.append("\"]");
        logger.trace("END toString()");
        return string.toString();
    }


    @Override
    public int size() {
        return this.stringList.size();
    }

    @Override
    public boolean isEmpty() {
        return this.stringList.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return this.stringList.contains(o);
    }

    public boolean contains(Attr attr) {
        return this.stringList.contains(attr);
    }

    @Override
    public Iterator iterator() {
        return this.stringList.iterator();
    }

    @Override
    public Object[] toArray() {
        return this.stringList.toArray();
    }

    @Override
    public Object[] toArray(Object[] a) {
        return this.stringList.toArray(a);
    }

    @Override
    public boolean add(String e) {
        return this.stringList.add(e);
    }

    @Override
    public boolean remove(Object o) {
        return this.stringList.remove(o);
    }

    public boolean remove(String value) {
        return this.stringList.remove(value);
    }

    @Override
    public boolean containsAll(Collection c) {
        return this.stringList.containsAll(c);
    }

    @Override
    public boolean addAll(Collection c) {
        return this.stringList.addAll(c);
    }

    @Override
    public boolean addAll(int index, Collection c) {
        return this.stringList.addAll(index, c);
    }

    @Override
    public boolean removeAll(Collection c) {
        return this.stringList.removeAll(c);
    }

    @Override
    public boolean retainAll(Collection c) {
        return this.stringList.retainAll(c);
    }

    @Override
    public void clear() {
        this.stringList.clear();
    }

    @Override
    public String get(int index) {
        return this.stringList.get(index);
    }

    @Override
    public String set(int index, String element) {
        return this.stringList.set(index, element);
    }

    @Override
    public void add(int index, String element) {
        this.stringList.add(index, element);
    }

    @Override
    public String remove(int index) {
        return this.stringList.remove(index);
    }

    @Override
    public int indexOf(Object o) {
        return this.stringList.indexOf(o);
    }

    public int indexOf(Attr attr) {
        return this.stringList.indexOf(attr);
    }

    @Override
    public int lastIndexOf(Object o) {
        return this.stringList.lastIndexOf(o);
    }

    public int lastIndexOf(String value) {
        return this.stringList.lastIndexOf(value);
    }

    @Override
    public ListIterator listIterator() {
        return this.stringList.listIterator();
    }

    @Override
    public ListIterator listIterator(int index) {
        return this.stringList.listIterator(index);
    }

    @Override
    public List subList(int fromIndex, int toIndex) {
        return this.stringList.subList(fromIndex, toIndex);
    }
}
